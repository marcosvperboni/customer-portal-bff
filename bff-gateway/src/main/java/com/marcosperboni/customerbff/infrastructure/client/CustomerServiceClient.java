package com.marcosperboni.customerbff.infrastructure.client;

import com.marcosperboni.customerbff.config.properties.ClientsProperties;
import com.marcosperboni.customerbff.domain.exception.CustomerNotFoundException;
import com.marcosperboni.customerbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.customerbff.domain.model.CustomerSummary;
import com.marcosperboni.customerbff.domain.port.CustomerClientPort;
import com.marcosperboni.customerbff.infrastructure.client.dto.CustomerUpstreamDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CustomerServiceClient implements CustomerClientPort {

    private final WebClient webClient;
    private final Duration timeout;

    public CustomerServiceClient(WebClient customerServiceWebClient, ClientsProperties properties) {
        this.webClient = customerServiceWebClient;
        this.timeout = Duration.ofMillis(properties.customerService().timeoutMillis());
    }

    @Override
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallback")
    @Retry(name = "customerService")
    public Mono<CustomerSummary> getCustomer(String customerId, String authorization) {
        return webClient.get()
                .uri("/api/customers/{id}", customerId)
                .headers(headers -> applyAuth(headers, authorization))
                .retrieve()
                .onStatus(status -> status.value() == 404, resp -> Mono.error(new CustomerNotFoundException(customerId)))
                .bodyToMono(CustomerUpstreamDto.class)
                .timeout(timeout)
                .map(CustomerUpstreamDto::toSummary);
    }

    @Override
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallback")
    @Retry(name = "customerService")
    public Mono<CustomerSummary> createCustomer(CustomerSummary customer, String authorization) {
        return webClient.post()
                .uri("/api/customers")
                .headers(headers -> applyAuth(headers, authorization))
                .bodyValue(CustomerUpstreamDto.WriteRequest.fromSummary(customer))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.createException())
                .bodyToMono(CustomerUpstreamDto.class)
                .timeout(timeout)
                .map(CustomerUpstreamDto::toSummary);
    }

    @Override
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallback")
    @Retry(name = "customerService")
    public Mono<CustomerSummary> updateCustomer(String customerId, CustomerSummary customer, String authorization) {
        return webClient.put()
                .uri("/api/customers/{id}", customerId)
                .headers(headers -> applyAuth(headers, authorization))
                .bodyValue(CustomerUpstreamDto.WriteRequest.fromSummary(customer))
                .retrieve()
                .onStatus(status -> status.value() == 404, resp -> Mono.error(new CustomerNotFoundException(customerId)))
                .bodyToMono(CustomerUpstreamDto.class)
                .timeout(timeout)
                .map(CustomerUpstreamDto::toSummary);
    }

    @Override
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallbackVoid")
    @Retry(name = "customerService")
    public Mono<Void> deleteCustomer(String customerId, String authorization) {
        return webClient.delete()
                .uri("/api/customers/{id}", customerId)
                .headers(headers -> applyAuth(headers, authorization))
                .retrieve()
                .onStatus(status -> status.value() == 404, resp -> Mono.error(new CustomerNotFoundException(customerId)))
                .toBodilessEntity()
                .timeout(timeout)
                .then();
    }

    private void applyAuth(HttpHeaders headers, String authorization) {
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    @SuppressWarnings("unused")
    private Mono<CustomerSummary> fallback(String customerId, String authorization, CustomerNotFoundException ex) {
        return Mono.error(ex);
    }

    @SuppressWarnings("unused")
    private Mono<CustomerSummary> fallback(String customerId, CustomerSummary customer, String authorization, CustomerNotFoundException ex) {
        return Mono.error(ex);
    }

    @SuppressWarnings("unused")
    private Mono<CustomerSummary> fallback(String customerId, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("customer-service", ex));
    }

    @SuppressWarnings("unused")
    private Mono<CustomerSummary> fallback(CustomerSummary customer, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("customer-service", ex));
    }

    @SuppressWarnings("unused")
    private Mono<CustomerSummary> fallback(String customerId, CustomerSummary customer, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("customer-service", ex));
    }

    @SuppressWarnings("unused")
    private Mono<Void> fallbackVoid(String customerId, String authorization, CustomerNotFoundException ex) {
        return Mono.error(ex);
    }

    @SuppressWarnings("unused")
    private Mono<Void> fallbackVoid(String customerId, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("customer-service", ex));
    }
}
