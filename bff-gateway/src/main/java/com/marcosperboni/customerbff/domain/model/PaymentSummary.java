package com.marcosperboni.customerbff.domain.model;

import java.math.BigDecimal;

public record PaymentSummary(String id, String orderId, BigDecimal amount, String method, String status) {
}
