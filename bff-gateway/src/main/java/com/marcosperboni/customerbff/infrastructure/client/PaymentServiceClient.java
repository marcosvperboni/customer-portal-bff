package com.marcosperboni.customerbff.infrastructure.client;

import com.marcosperboni.customerbff.config.properties.ClientsProperties;
import com.marcosperboni.customerbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.customerbff.domain.model.PaymentSummary;
import com.marcosperboni.customerbff.domain.port.PaymentClientPort;
import com.marcosperboni.customerbff.infrastructure.client.dto.PaymentUpstreamDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentServiceClient implements PaymentClientPort {

    private final WebClient webClient;
    private final Duration timeout;

    public PaymentServiceClient(WebClient paymentServiceWebClient, ClientsProperties properties) {
        this.webClient = paymentServiceWebClient;
        this.timeout = Duration.ofMillis(properties.paymentService().timeoutMillis());
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
    @Retry(name = "paymentService")
    public Mono<List<PaymentSummary>> getPaymentsByCustomer(String customerId, String authorization) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/payments").queryParam("customerId", customerId).build())
                .headers(headers -> {
                    if (authorization != null && !authorization.isBlank()) {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                    }
                })
                .retrieve()
                .bodyToFlux(PaymentUpstreamDto.class)
                .map(PaymentUpstreamDto::toSummary)
                .collectList()
                .timeout(timeout);
    }

    @SuppressWarnings("unused")
    private Mono<List<PaymentSummary>> fallback(String customerId, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("payment-service", ex));
    }
}
