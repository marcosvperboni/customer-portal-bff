package com.marcosperboni.customerbff.web.dto;

import com.marcosperboni.customerbff.domain.model.PaymentSummary;
import java.math.BigDecimal;

public record PaymentResponse(String id, String orderId, BigDecimal amount, String method, String status) {

    public static PaymentResponse from(PaymentSummary summary) {
        return new PaymentResponse(summary.id(), summary.orderId(), summary.amount(), summary.method(), summary.status());
    }
}
