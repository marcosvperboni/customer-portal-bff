package com.marcosperboni.paymentservice.web.dto;

import com.marcosperboni.paymentservice.domain.PaymentMethod;
import com.marcosperboni.paymentservice.domain.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank(message = "customerId is required") String customerId,
        @NotBlank(message = "orderId is required") String orderId,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than zero") BigDecimal amount,
        @NotNull(message = "method is required") PaymentMethod method,
        PaymentStatus status) {
}
