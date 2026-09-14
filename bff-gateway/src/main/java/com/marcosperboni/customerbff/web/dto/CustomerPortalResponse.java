package com.marcosperboni.customerbff.web.dto;

import com.marcosperboni.customerbff.domain.model.CustomerPortalView;
import java.util.List;

public record CustomerPortalResponse(
        CustomerResponse customer,
        List<OrderResponse> orders,
        List<PaymentResponse> payments,
        List<String> warnings) {

    public static CustomerPortalResponse from(CustomerPortalView view) {
        return new CustomerPortalResponse(
                CustomerResponse.from(view.customer()),
                view.orders().stream().map(OrderResponse::from).toList(),
                view.payments().stream().map(PaymentResponse::from).toList(),
                view.warnings());
    }
}
