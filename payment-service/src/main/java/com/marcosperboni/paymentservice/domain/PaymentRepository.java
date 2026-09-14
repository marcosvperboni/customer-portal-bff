package com.marcosperboni.paymentservice.domain;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    List<Payment> findAll();

    List<Payment> findByCustomerId(String customerId);

    Optional<Payment> findById(String id);

    Payment save(Payment payment);

    boolean deleteById(String id);
}
