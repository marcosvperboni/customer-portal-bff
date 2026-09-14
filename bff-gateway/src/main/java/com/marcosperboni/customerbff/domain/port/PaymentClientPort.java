package com.marcosperboni.customerbff.domain.port;

import com.marcosperboni.customerbff.domain.model.PaymentSummary;
import java.util.List;
import reactor.core.publisher.Mono;

public interface PaymentClientPort {

    Mono<List<PaymentSummary>> getPaymentsByCustomer(String customerId, String authorization);
}
