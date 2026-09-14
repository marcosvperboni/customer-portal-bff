package com.marcosperboni.customerbff.application;

import com.marcosperboni.customerbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.customerbff.domain.model.CustomerPortalView;
import com.marcosperboni.customerbff.domain.model.OrderSummary;
import com.marcosperboni.customerbff.domain.model.PaymentSummary;
import com.marcosperboni.customerbff.domain.port.CustomerClientPort;
import com.marcosperboni.customerbff.domain.port.OrderClientPort;
import com.marcosperboni.customerbff.domain.port.PaymentClientPort;
import com.marcosperboni.customerbff.infrastructure.cache.PortalCacheService;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Orchestrates the three downstream calls behind a single response. The
 * customer lookup is treated as mandatory - if it fails the whole call fails,
 * since a portal with no customer identity is meaningless. Orders and
 * payments degrade gracefully to an empty list plus a warning so a partial
 * outage never turns into a hard error for the frontend.
 */
@Service
public class CustomerPortalService {

    private final CustomerClientPort customerClient;
    private final OrderClientPort orderClient;
    private final PaymentClientPort paymentClient;
    private final PortalCacheService cacheService;

    public CustomerPortalService(CustomerClientPort customerClient, OrderClientPort orderClient,
            PaymentClientPort paymentClient, PortalCacheService cacheService) {
        this.customerClient = customerClient;
        this.orderClient = orderClient;
        this.paymentClient = paymentClient;
        this.cacheService = cacheService;
    }

    public Mono<CustomerPortalView> getPortal(String customerId, String authorization) {
        return cacheService.get(customerId)
                .switchIfEmpty(Mono.defer(() -> buildPortal(customerId, authorization)
                        .flatMap(view -> cacheService.put(customerId, view).thenReturn(view))));
    }

    private Mono<CustomerPortalView> buildPortal(String customerId, String authorization) {
        List<String> warnings = new CopyOnWriteArrayList<>();

        Mono<List<OrderSummary>> orders = orderClient.getOrdersByCustomer(customerId, authorization)
                .onErrorResume(DownstreamUnavailableException.class, ex -> {
                    warnings.add("orders unavailable: " + ex.getMessage());
                    return Mono.just(List.of());
                });

        Mono<List<PaymentSummary>> payments = paymentClient.getPaymentsByCustomer(customerId, authorization)
                .onErrorResume(DownstreamUnavailableException.class, ex -> {
                    warnings.add("payments unavailable: " + ex.getMessage());
                    return Mono.just(List.of());
                });

        return Mono.zip(customerClient.getCustomer(customerId, authorization), orders, payments)
                .map(tuple -> new CustomerPortalView(tuple.getT1(), tuple.getT2(), tuple.getT3(), List.copyOf(warnings)));
    }
}
