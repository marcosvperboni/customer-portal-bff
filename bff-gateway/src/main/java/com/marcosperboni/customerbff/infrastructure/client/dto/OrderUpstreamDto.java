package com.marcosperboni.customerbff.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.marcosperboni.customerbff.domain.model.OrderSummary;
import java.math.BigDecimal;

/** Mirrors order-service's OrderResponse payload shape. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderUpstreamDto(String id, String customerId, String description, BigDecimal amount, String status) {

    public OrderSummary toSummary() {
        return new OrderSummary(id, description, amount, status);
    }
}
