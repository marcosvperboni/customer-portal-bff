package com.marcosperboni.customerbff.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcosperboni.customerbff.domain.exception.CustomerNotFoundException;
import com.marcosperboni.customerbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.customerbff.domain.model.CustomerPortalView;
import com.marcosperboni.customerbff.domain.model.CustomerSummary;
import com.marcosperboni.customerbff.domain.model.OrderSummary;
import com.marcosperboni.customerbff.domain.model.PaymentSummary;
import com.marcosperboni.customerbff.domain.port.CustomerClientPort;
import com.marcosperboni.customerbff.domain.port.OrderClientPort;
import com.marcosperboni.customerbff.domain.port.PaymentClientPort;
import com.marcosperboni.customerbff.infrastructure.cache.PortalCacheService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CustomerPortalServiceTest {

    @Mock
    private CustomerClientPort customerClient;
    @Mock
    private OrderClientPort orderClient;
    @Mock
    private PaymentClientPort paymentClient;
    @Mock
    private PortalCacheService cacheService;

    private CustomerPortalService service;

    private static final CustomerSummary CUSTOMER = new CustomerSummary("cust-1", "Ada", "ada@example.com", "doc", "phone");

    @BeforeEach
    void setUp() {
        service = new CustomerPortalService(customerClient, orderClient, paymentClient, cacheService);
        org.mockito.Mockito.lenient().when(cacheService.get(anyString())).thenReturn(Mono.empty());
        org.mockito.Mockito.lenient().when(cacheService.put(anyString(), any())).thenReturn(Mono.just(true));
    }

    @Test
    void aggregatesCustomerOrdersAndPaymentsWhenAllAvailable() {
        when(customerClient.getCustomer("cust-1", null)).thenReturn(Mono.just(CUSTOMER));
        when(orderClient.getOrdersByCustomer("cust-1", null)).thenReturn(
                Mono.just(List.of(new OrderSummary("order-1", "Item", new BigDecimal("10.00"), "CREATED"))));
        when(paymentClient.getPaymentsByCustomer("cust-1", null)).thenReturn(
                Mono.just(List.of(new PaymentSummary("pay-1", "order-1", new BigDecimal("10.00"), "PIX", "PENDING"))));

        StepVerifier.create(service.getPortal("cust-1", null))
                .assertNext(view -> {
                    assertThat(view.customer()).isEqualTo(CUSTOMER);
                    assertThat(view.orders()).hasSize(1);
                    assertThat(view.payments()).hasSize(1);
                    assertThat(view.warnings()).isEmpty();
                })
                .verifyComplete();

        verify(cacheService).put(anyString(), any(CustomerPortalView.class));
    }

    @Test
    void returnsCachedViewWithoutCallingDownstreams() {
        CustomerPortalView cached = new CustomerPortalView(CUSTOMER, List.of(), List.of(), List.of());
        when(cacheService.get("cust-1")).thenReturn(Mono.just(cached));

        StepVerifier.create(service.getPortal("cust-1", null))
                .expectNext(cached)
                .verifyComplete();

        org.mockito.Mockito.verifyNoInteractions(customerClient, orderClient, paymentClient);
    }

    @Test
    void degradesGracefullyWhenOrdersServiceIsUnavailable() {
        when(customerClient.getCustomer("cust-1", null)).thenReturn(Mono.just(CUSTOMER));
        when(orderClient.getOrdersByCustomer("cust-1", null))
                .thenReturn(Mono.error(new DownstreamUnavailableException("order-service", new RuntimeException("timeout"))));
        when(paymentClient.getPaymentsByCustomer("cust-1", null)).thenReturn(Mono.just(List.of()));

        StepVerifier.create(service.getPortal("cust-1", null))
                .assertNext(view -> {
                    assertThat(view.orders()).isEmpty();
                    assertThat(view.warnings()).hasSize(1);
                    assertThat(view.warnings().get(0)).contains("orders unavailable");
                })
                .verifyComplete();
    }

    @Test
    void propagatesErrorWhenCustomerIsNotFound() {
        when(customerClient.getCustomer("missing", null)).thenReturn(Mono.error(new CustomerNotFoundException("missing")));
        when(orderClient.getOrdersByCustomer("missing", null)).thenReturn(Mono.just(List.of()));
        when(paymentClient.getPaymentsByCustomer("missing", null)).thenReturn(Mono.just(List.of()));

        StepVerifier.create(service.getPortal("missing", null))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }
}
