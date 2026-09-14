package com.marcosperboni.paymentservice.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Payment(
        String id,
        String customerId,
        String orderId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public Payment withUpdatedFields(BigDecimal amount, PaymentMethod method, PaymentStatus status) {
        return new Payment(this.id, this.customerId, this.orderId, amount, method, status, this.createdAt, Instant.now());
    }
}
