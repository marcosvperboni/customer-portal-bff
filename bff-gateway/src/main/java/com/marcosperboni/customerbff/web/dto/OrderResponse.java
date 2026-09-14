package com.marcosperboni.customerbff.web.dto;

import com.marcosperboni.customerbff.domain.model.OrderSummary;
import java.math.BigDecimal;

public record OrderResponse(String id, String description, BigDecimal amount, String status) {

    public static OrderResponse from(OrderSummary summary) {
        return new OrderResponse(summary.id(), summary.description(), summary.amount(), summary.status());
    }
}
