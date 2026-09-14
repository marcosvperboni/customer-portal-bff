package com.marcosperboni.orderservice.domain;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    List<Order> findAll();

    List<Order> findByCustomerId(String customerId);

    Optional<Order> findById(String id);

    Order save(Order order);

    boolean deleteById(String id);
}
