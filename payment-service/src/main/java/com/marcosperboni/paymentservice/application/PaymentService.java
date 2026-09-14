package com.marcosperboni.paymentservice.application;

import com.marcosperboni.paymentservice.domain.Payment;
import com.marcosperboni.paymentservice.domain.PaymentMethod;
import com.marcosperboni.paymentservice.domain.PaymentNotFoundException;
import com.marcosperboni.paymentservice.domain.PaymentRepository;
import com.marcosperboni.paymentservice.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public List<Payment> findAll() {
        return repository.findAll();
    }

    public List<Payment> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    public Payment findById(String id) {
        return repository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    public Payment create(String customerId, String orderId, BigDecimal amount, PaymentMethod method) {
        Instant now = Instant.now();
        Payment payment = new Payment("pay-" + UUID.randomUUID(), customerId, orderId, amount, method,
                PaymentStatus.PENDING, now, now);
        return repository.save(payment);
    }

    public Payment update(String id, BigDecimal amount, PaymentMethod method, PaymentStatus status) {
        Payment existing = findById(id);
        Payment updated = existing.withUpdatedFields(amount, method, status);
        return repository.save(updated);
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new PaymentNotFoundException(id);
        }
    }
}
