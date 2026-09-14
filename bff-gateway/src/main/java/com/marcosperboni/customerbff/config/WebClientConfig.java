package com.marcosperboni.customerbff.config;

import com.marcosperboni.customerbff.config.properties.ClientsProperties;
import com.marcosperboni.customerbff.web.filter.CorrelationIdWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * One WebClient per downstream service. Each carries the correlation-id
 * propagation filter so distributed traces stay linked without callers
 * having to remember to add the header themselves.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient customerServiceWebClient(ClientsProperties properties) {
        return buildClient(properties.customerService().baseUrl());
    }

    @Bean
    public WebClient orderServiceWebClient(ClientsProperties properties) {
        return buildClient(properties.orderService().baseUrl());
    }

    @Bean
    public WebClient paymentServiceWebClient(ClientsProperties properties) {
        return buildClient(properties.paymentService().baseUrl());
    }

    private WebClient buildClient(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(correlationIdPropagationFilter())
                .build();
    }

    private ExchangeFilterFunction correlationIdPropagationFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> Mono.deferContextual(ctx -> {
            String correlationId = ctx.getOrDefault(CorrelationIdWebFilter.CONTEXT_KEY, "n/a");
            ClientRequest withHeader = ClientRequest.from(request)
                    .header(CorrelationIdWebFilter.HEADER_NAME, correlationId)
                    .build();
            return Mono.just(withHeader);
        }));
    }
}
