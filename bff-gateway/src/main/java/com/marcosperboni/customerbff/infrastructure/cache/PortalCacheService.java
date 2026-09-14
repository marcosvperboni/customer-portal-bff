package com.marcosperboni.customerbff.infrastructure.cache;

import com.marcosperboni.customerbff.config.properties.CacheProperties;
import com.marcosperboni.customerbff.domain.model.CustomerPortalView;
import java.time.Duration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Caches the aggregated portal view in Redis so a burst of requests for the
 * same customer doesn't hammer three downstream services. Serialized as JSON
 * strings via the app's own Jackson ObjectMapper - avoids pulling in a
 * separate Jackson2-based Redis serializer just for this.
 */
@Component
public class PortalCacheService {

    private static final String KEY_PREFIX = "portal:customer:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public PortalCacheService(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper,
            CacheProperties cacheProperties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(cacheProperties.portalTtlSeconds());
    }

    public Mono<CustomerPortalView> get(String customerId) {
        return redisTemplate.opsForValue().get(key(customerId))
                .map(json -> objectMapper.readValue(json, CustomerPortalView.class))
                .onErrorResume(ex -> Mono.empty());
    }

    public Mono<Boolean> put(String customerId, CustomerPortalView view) {
        String json = objectMapper.writeValueAsString(view);
        return redisTemplate.opsForValue().set(key(customerId), json, ttl)
                .onErrorResume(ex -> Mono.just(false));
    }

    public Mono<Boolean> evict(String customerId) {
        return redisTemplate.opsForValue().delete(key(customerId))
                .onErrorResume(ex -> Mono.just(false));
    }

    private String key(String customerId) {
        return KEY_PREFIX + customerId;
    }
}
