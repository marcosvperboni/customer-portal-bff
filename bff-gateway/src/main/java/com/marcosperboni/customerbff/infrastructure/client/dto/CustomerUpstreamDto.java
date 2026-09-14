package com.marcosperboni.customerbff.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.marcosperboni.customerbff.domain.model.CustomerSummary;

/** Mirrors customer-service's CustomerRequest/CustomerResponse payload shape. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerUpstreamDto(String id, String name, String email, String document, String phone) {

    public CustomerSummary toSummary() {
        return new CustomerSummary(id, name, email, document, phone);
    }

    public static CustomerUpstreamDto fromSummary(CustomerSummary summary) {
        return new CustomerUpstreamDto(summary.id(), summary.name(), summary.email(), summary.document(), summary.phone());
    }

    /** customer-service's write payload has no id field - it is assigned or resolved from the path. */
    public record WriteRequest(String name, String email, String document, String phone) {

        public static WriteRequest fromSummary(CustomerSummary summary) {
            return new WriteRequest(summary.name(), summary.email(), summary.document(), summary.phone());
        }
    }
}
