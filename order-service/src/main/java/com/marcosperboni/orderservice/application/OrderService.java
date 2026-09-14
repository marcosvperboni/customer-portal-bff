package com.marcosperboni.orderservice.application;

import com.marcosperboni.orderservice.domain.Order;
import com.marcosperboni.orderservice.domain.OrderNotFoundException;
import com.marcosperboni.orderservice.domain.OrderRepository;
import com.marcosperboni.orderservice.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<Order> findAll() {
        return repository.findAll();
    }

    public List<Order> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    public Order findById(String id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    public Order create(String customerId, String description, BigDecimal amount) {
        Instant now = Instant.now();
        Order order = new Order("order-" + UUID.randomUUID(), customerId, description, amount,
                OrderStatus.CREATED, now, now);
        return repository.save(order);
    }

    public Order update(String id, String description, BigDecimal amount, OrderStatus status) {
        Order existing = findById(id);
        Order updated = existing.withUpdatedFields(description, amount, status);
        return repository.save(updated);
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new OrderNotFoundException(id);
        }
    }
}
