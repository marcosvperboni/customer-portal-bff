package com.marcosperboni.customerbff.web.dto;

import com.marcosperboni.customerbff.domain.model.CustomerSummary;

public record CustomerResponse(String id, String name, String email, String document, String phone) {

    public static CustomerResponse from(CustomerSummary summary) {
        return new CustomerResponse(summary.id(), summary.name(), summary.email(), summary.document(), summary.phone());
    }
}
