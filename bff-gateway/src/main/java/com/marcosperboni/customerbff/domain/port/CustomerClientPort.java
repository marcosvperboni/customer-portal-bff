package com.marcosperboni.customerbff.domain.port;

import com.marcosperboni.customerbff.domain.model.CustomerSummary;
import reactor.core.publisher.Mono;

public interface CustomerClientPort {

    Mono<CustomerSummary> getCustomer(String customerId, String authorization);

    Mono<CustomerSummary> createCustomer(CustomerSummary customer, String authorization);

    Mono<CustomerSummary> updateCustomer(String customerId, CustomerSummary customer, String authorization);

    Mono<Void> deleteCustomer(String customerId, String authorization);
}
