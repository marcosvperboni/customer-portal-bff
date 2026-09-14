package com.marcosperboni.orderservice.web.dto;

import com.marcosperboni.orderservice.domain.Order;
import com.marcosperboni.orderservice.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String id,
        String customerId,
        String description,
        BigDecimal amount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.id(), order.customerId(), order.description(), order.amount(),
                order.status(), order.createdAt(), order.updatedAt());
    }
}
