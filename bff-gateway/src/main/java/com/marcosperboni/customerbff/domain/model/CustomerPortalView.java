package com.marcosperboni.customerbff.domain.model;

import java.util.List;

/**
 * Aggregate returned by the portal endpoint. {@code warnings} lists which
 * downstream services degraded to a fallback so the frontend can render
 * partial data instead of a hard failure.
 */
public record CustomerPortalView(
        CustomerSummary customer,
        List<OrderSummary> orders,
        List<PaymentSummary> payments,
        List<String> warnings) {
}
