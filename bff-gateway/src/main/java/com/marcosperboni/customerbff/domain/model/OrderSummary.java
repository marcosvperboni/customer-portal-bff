package com.marcosperboni.customerbff.domain.model;

import java.math.BigDecimal;

public record OrderSummary(String id, String description, BigDecimal amount, String status) {
}
