package com.marcosperboni.customerservice.application;

import com.marcosperboni.customerservice.domain.Customer;
import com.marcosperboni.customerservice.domain.CustomerNotFoundException;
import com.marcosperboni.customerservice.domain.CustomerRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<Customer> findAll() {
        return repository.findAll();
    }

    public Customer findById(String id) {
        return repository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public Customer create(String name, String email, String document, String phone) {
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        Instant now = Instant.now();
        Customer customer = new Customer("cust-" + UUID.randomUUID(), name, email, document, phone, now, now);
        return repository.save(customer);
    }

    public Customer update(String id, String name, String email, String document, String phone) {
        Customer existing = findById(id);
        Customer updated = existing.withUpdatedFields(name, email, document, phone);
        return repository.save(updated);
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new CustomerNotFoundException(id);
        }
    }
}
