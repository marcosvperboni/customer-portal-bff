package com.marcosperboni.orderservice.web.dto;

import com.marcosperboni.orderservice.domain.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank(message = "customerId is required") String customerId,
        @NotBlank(message = "description is required") String description,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than zero") BigDecimal amount,
        OrderStatus status) {
}
