package com.marcosperboni.orderservice.infrastructure;

import com.marcosperboni.orderservice.domain.Order;
import com.marcosperboni.orderservice.domain.OrderRepository;
import com.marcosperboni.orderservice.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory store standing in for a real persistence layer, mirroring
 * customer-service's approach - the point of this mock service is to exercise
 * the BFF's orchestration and resilience layer, not persistence.
 */
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public InMemoryOrderRepository() {
        seed();
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return store.values().stream().filter(o -> o.customerId().equals(customerId)).toList();
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Order save(Order order) {
        store.put(order.id(), order);
        return order;
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    private void seed() {
        Instant now = Instant.now();
        save(new Order("order-2001", "cust-1001", "Mechanical keyboard", new BigDecimal("450.00"),
                OrderStatus.PAID, now, now));
        save(new Order("order-2002", "cust-1001", "27\" monitor", new BigDecimal("1290.00"),
                OrderStatus.SHIPPED, now, now));
        save(new Order("order-2003", "cust-1002", "Noise cancelling headset", new BigDecimal("890.00"),
                OrderStatus.CREATED, now, now));
    }
}
