package com.marcosperboni.orderservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marcosperboni.orderservice.domain.Order;
import com.marcosperboni.orderservice.domain.OrderNotFoundException;
import com.marcosperboni.orderservice.domain.OrderStatus;
import com.marcosperboni.orderservice.infrastructure.InMemoryOrderRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(new InMemoryOrderRepository());
    }

    @Test
    void createsOrderInCreatedStatus() {
        Order created = service.create("cust-1001", "Wireless mouse", new BigDecimal("120.00"));

        assertThat(created.id()).startsWith("order-");
        assertThat(created.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void findsOrdersByCustomerId() {
        service.create("cust-9999", "Test item", new BigDecimal("10.00"));

        assertThat(service.findByCustomerId("cust-9999")).hasSize(1);
        assertThat(service.findByCustomerId("cust-1001")).isNotEmpty();
    }

    @Test
    void throwsNotFoundForUnknownOrder() {
        assertThatThrownBy(() -> service.findById("does-not-exist")).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updatesOrderStatus() {
        Order created = service.create("cust-1001", "Item", new BigDecimal("10.00"));

        Order updated = service.update(created.id(), "Item", new BigDecimal("10.00"), OrderStatus.SHIPPED);

        assertThat(updated.status()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void deletesExistingOrder() {
        Order created = service.create("cust-1001", "Item", new BigDecimal("10.00"));

        service.delete(created.id());

        assertThatThrownBy(() -> service.findById(created.id())).isInstanceOf(OrderNotFoundException.class);
    }
}
