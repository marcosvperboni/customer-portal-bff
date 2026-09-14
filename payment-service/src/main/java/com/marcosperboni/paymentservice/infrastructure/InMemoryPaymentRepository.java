package com.marcosperboni.paymentservice.infrastructure;

import com.marcosperboni.paymentservice.domain.Payment;
import com.marcosperboni.paymentservice.domain.PaymentMethod;
import com.marcosperboni.paymentservice.domain.PaymentRepository;
import com.marcosperboni.paymentservice.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory store standing in for a real persistence layer, mirroring the
 * other mock services - this API exists to exercise the BFF's orchestration
 * and resilience layer, not persistence.
 */
@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> store = new ConcurrentHashMap<>();

    public InMemoryPaymentRepository() {
        seed();
    }

    @Override
    public List<Payment> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<Payment> findByCustomerId(String customerId) {
        return store.values().stream().filter(p -> p.customerId().equals(customerId)).toList();
    }

    @Override
    public Optional<Payment> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Payment save(Payment payment) {
        store.put(payment.id(), payment);
        return payment;
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    private void seed() {
        Instant now = Instant.now();
        save(new Payment("pay-3001", "cust-1001", "order-2001", new BigDecimal("450.00"),
                PaymentMethod.PIX, PaymentStatus.APPROVED, now, now));
        save(new Payment("pay-3002", "cust-1001", "order-2002", new BigDecimal("1290.00"),
                PaymentMethod.CREDIT_CARD, PaymentStatus.APPROVED, now, now));
        save(new Payment("pay-3003", "cust-1002", "order-2003", new BigDecimal("890.00"),
                PaymentMethod.BOLETO, PaymentStatus.PENDING, now, now));
    }
}
