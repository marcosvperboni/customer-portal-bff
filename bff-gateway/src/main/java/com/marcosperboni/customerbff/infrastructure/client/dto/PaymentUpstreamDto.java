package com.marcosperboni.customerbff.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.marcosperboni.customerbff.domain.model.PaymentSummary;
import java.math.BigDecimal;

/** Mirrors payment-service's PaymentResponse payload shape. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentUpstreamDto(
        String id, String customerId, String orderId, BigDecimal amount, String method, String status) {

    public PaymentSummary toSummary() {
        return new PaymentSummary(id, orderId, amount, method, status);
    }
}
