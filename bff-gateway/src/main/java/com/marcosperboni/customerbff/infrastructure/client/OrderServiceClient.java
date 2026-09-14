package com.marcosperboni.customerbff.infrastructure.client;

import com.marcosperboni.customerbff.config.properties.ClientsProperties;
import com.marcosperboni.customerbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.customerbff.domain.model.OrderSummary;
import com.marcosperboni.customerbff.domain.port.OrderClientPort;
import com.marcosperboni.customerbff.infrastructure.client.dto.OrderUpstreamDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OrderServiceClient implements OrderClientPort {

    private final WebClient webClient;
    private final Duration timeout;

    public OrderServiceClient(WebClient orderServiceWebClient, ClientsProperties properties) {
        this.webClient = orderServiceWebClient;
        this.timeout = Duration.ofMillis(properties.orderService().timeoutMillis());
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public Mono<List<OrderSummary>> getOrdersByCustomer(String customerId, String authorization) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/orders").queryParam("customerId", customerId).build())
                .headers(headers -> {
                    if (authorization != null && !authorization.isBlank()) {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                    }
                })
                .retrieve()
                .bodyToFlux(OrderUpstreamDto.class)
                .map(OrderUpstreamDto::toSummary)
                .collectList()
                .timeout(timeout);
    }

    @SuppressWarnings("unused")
    private Mono<List<OrderSummary>> fallback(String customerId, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("order-service", ex));
    }
}
