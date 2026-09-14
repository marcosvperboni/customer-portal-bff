package com.marcosperboni.customerservice.domain;

import java.time.Instant;

public record Customer(
        String id,
        String name,
        String email,
        String document,
        String phone,
        Instant createdAt,
        Instant updatedAt) {

    public Customer withUpdatedFields(String name, String email, String document, String phone) {
        return new Customer(this.id, name, email, document, phone, this.createdAt, Instant.now());
    }
}
