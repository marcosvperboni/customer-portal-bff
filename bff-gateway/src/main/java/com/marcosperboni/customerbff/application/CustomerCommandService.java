package com.marcosperboni.customerbff.application;

import com.marcosperboni.customerbff.domain.model.CustomerSummary;
import com.marcosperboni.customerbff.domain.port.CustomerClientPort;
import com.marcosperboni.customerbff.infrastructure.cache.PortalCacheService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomerCommandService {

    private final CustomerClientPort customerClient;
    private final PortalCacheService cacheService;

    public CustomerCommandService(CustomerClientPort customerClient, PortalCacheService cacheService) {
        this.customerClient = customerClient;
        this.cacheService = cacheService;
    }

    public Mono<CustomerSummary> create(CustomerSummary customer, String authorization) {
        return customerClient.createCustomer(customer, authorization);
    }

    public Mono<CustomerSummary> update(String customerId, CustomerSummary customer, String authorization) {
        return customerClient.updateCustomer(customerId, customer, authorization)
                .flatMap(updated -> cacheService.evict(customerId).thenReturn(updated));
    }

    public Mono<Void> delete(String customerId, String authorization) {
        return customerClient.deleteCustomer(customerId, authorization)
                .then(cacheService.evict(customerId))
                .then();
    }
}
