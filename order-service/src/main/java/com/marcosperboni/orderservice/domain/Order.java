package com.marcosperboni.orderservice.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(
        String id,
        String customerId,
        String description,
        BigDecimal amount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public Order withUpdatedFields(String description, BigDecimal amount, OrderStatus status) {
        return new Order(this.id, this.customerId, description, amount, status, this.createdAt, Instant.now());
    }
}
