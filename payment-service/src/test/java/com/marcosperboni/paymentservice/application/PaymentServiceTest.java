package com.marcosperboni.paymentservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marcosperboni.paymentservice.domain.Payment;
import com.marcosperboni.paymentservice.domain.PaymentMethod;
import com.marcosperboni.paymentservice.domain.PaymentNotFoundException;
import com.marcosperboni.paymentservice.domain.PaymentStatus;
import com.marcosperboni.paymentservice.infrastructure.InMemoryPaymentRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(new InMemoryPaymentRepository());
    }

    @Test
    void createsPaymentInPendingStatus() {
        Payment created = service.create("cust-1001", "order-2001", new BigDecimal("99.90"), PaymentMethod.PIX);

        assertThat(created.id()).startsWith("pay-");
        assertThat(created.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void findsPaymentsByCustomerId() {
        service.create("cust-9999", "order-9999", new BigDecimal("10.00"), PaymentMethod.PIX);

        assertThat(service.findByCustomerId("cust-9999")).hasSize(1);
        assertThat(service.findByCustomerId("cust-1001")).isNotEmpty();
    }

    @Test
    void throwsNotFoundForUnknownPayment() {
        assertThatThrownBy(() -> service.findById("does-not-exist")).isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void updatesPaymentStatus() {
        Payment created = service.create("cust-1001", "order-2001", new BigDecimal("10.00"), PaymentMethod.PIX);

        Payment updated = service.update(created.id(), created.amount(), created.method(), PaymentStatus.APPROVED);

        assertThat(updated.status()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    void deletesExistingPayment() {
        Payment created = service.create("cust-1001", "order-2001", new BigDecimal("10.00"), PaymentMethod.PIX);

        service.delete(created.id());

        assertThatThrownBy(() -> service.findById(created.id())).isInstanceOf(PaymentNotFoundException.class);
    }
}
