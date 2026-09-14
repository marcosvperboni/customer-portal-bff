package com.marcosperboni.customerservice.domain;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {

    List<Customer> findAll();

    Optional<Customer> findById(String id);

    Customer save(Customer customer);

    boolean deleteById(String id);

    boolean existsByEmail(String email);
}
