package com.marcosperboni.customerservice.web.dto;

import com.marcosperboni.customerservice.domain.Customer;
import java.time.Instant;

public record CustomerResponse(
        String id,
        String name,
        String email,
        String document,
        String phone,
        Instant createdAt,
        Instant updatedAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.id(), customer.name(), customer.email(),
                customer.document(), customer.phone(), customer.createdAt(), customer.updatedAt());
    }
}
