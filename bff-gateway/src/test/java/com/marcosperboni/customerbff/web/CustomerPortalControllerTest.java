package com.marcosperboni.customerbff.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.marcosperboni.customerbff.application.CustomerCommandService;
import com.marcosperboni.customerbff.application.CustomerPortalService;
import com.marcosperboni.customerbff.domain.exception.CustomerNotFoundException;
import com.marcosperboni.customerbff.domain.model.CustomerPortalView;
import com.marcosperboni.customerbff.domain.model.CustomerSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = CustomerPortalController.class, excludeAutoConfiguration = ReactiveWebSecurityAutoConfiguration.class)
class CustomerPortalControllerTest {

    private static final CustomerSummary CUSTOMER = new CustomerSummary("cust-1", "Ada", "ada@example.com", "doc", "phone");

    @org.springframework.beans.factory.annotation.Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerPortalService portalService;

    @MockitoBean
    private CustomerCommandService commandService;

    @Test
    void getPortalReturnsAggregatedView() {
        CustomerPortalView view = new CustomerPortalView(CUSTOMER, List.of(), List.of(), List.of());
        given(portalService.getPortal(eq("cust-1"), any())).willReturn(reactor.core.publisher.Mono.just(view));

        webTestClient.get().uri("/api/portal/customers/cust-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.customer.id").isEqualTo("cust-1")
                .jsonPath("$.warnings").isArray();
    }

    @Test
    void getPortalReturns404WhenCustomerMissing() {
        given(portalService.getPortal(eq("missing"), any()))
                .willReturn(reactor.core.publisher.Mono.error(new CustomerNotFoundException("missing")));

        webTestClient.get().uri("/api/portal/customers/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("NOT_FOUND");
    }

    @Test
    void createRejectsInvalidPayload() {
        webTestClient.post().uri("/api/portal/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new com.marcosperboni.customerbff.web.dto.CustomerCommandRequest("", "not-an-email", "", ""))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void createReturns201() {
        given(commandService.create(any(), any())).willReturn(reactor.core.publisher.Mono.just(CUSTOMER));

        webTestClient.post().uri("/api/portal/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new com.marcosperboni.customerbff.web.dto.CustomerCommandRequest("Ada", "ada@example.com", "doc", "phone"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("cust-1");
    }

    @Test
    void deleteReturnsNoContent() {
        given(commandService.delete(eq("cust-1"), any())).willReturn(reactor.core.publisher.Mono.empty());

        webTestClient.delete().uri("/api/portal/customers/cust-1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
