package com.marcosperboni.paymentservice.web.dto;

import com.marcosperboni.paymentservice.domain.Payment;
import com.marcosperboni.paymentservice.domain.PaymentMethod;
import com.marcosperboni.paymentservice.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String customerId,
        String orderId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.id(), payment.customerId(), payment.orderId(), payment.amount(),
                payment.method(), payment.status(), payment.createdAt(), payment.updatedAt());
    }
}
