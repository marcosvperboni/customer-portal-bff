package com.marcosperboni.customerbff.domain.port;

import com.marcosperboni.customerbff.domain.model.OrderSummary;
import java.util.List;
import reactor.core.publisher.Mono;

public interface OrderClientPort {

    Mono<List<OrderSummary>> getOrdersByCustomer(String customerId, String authorization);
}
