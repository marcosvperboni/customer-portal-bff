package com.marcosperboni.customerbff.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.marcosperboni.customerbff.domain.exception.DownstreamUnavailableException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;

/**
 * Exercises the real Resilience4j retry + circuit breaker wiring against a
 * WireMock stand-in for order-service, instead of just asserting the
 * annotations are present.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OrderServiceClientResilienceTest {

    static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @Autowired
    private OrderServiceClient orderServiceClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void registerDownstreamUrl(DynamicPropertyRegistry registry) {
        registry.add("app.clients.order-service.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void resetStubs() {
        wireMockServer.resetAll();
    }

    @Test
    void retriesOnFailureThenFallsBackToDownstreamUnavailable() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/orders")).willReturn(aResponse().withStatus(500)));

        StepVerifier.create(orderServiceClient.getOrdersByCustomer("cust-1001", null))
                .expectErrorMatches(DownstreamUnavailableException.class::isInstance)
                .verify();

        wireMockServer.verify(moreThanOrExactly(2), getRequestedFor(urlPathEqualTo("/api/orders")));
    }

    @Test
    void succeedsWithoutRetryWhenDownstreamIsHealthy() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/orders")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        StepVerifier.create(orderServiceClient.getOrdersByCustomer("cust-1001", null))
                .assertNext(orders -> org.assertj.core.api.Assertions.assertThat(orders).isEmpty())
                .verifyComplete();
    }
}
