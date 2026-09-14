package com.marcosperboni.customerservice.infrastructure;

import com.marcosperboni.customerservice.domain.Customer;
import com.marcosperboni.customerservice.domain.CustomerRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory store standing in for a real persistence layer. This service exists to
 * demonstrate the BFF orchestration pattern, not database design, so a
 * ConcurrentHashMap is enough - swap for a JPA/R2DBC repository if this ever
 * needs to survive a restart.
 */
@Repository
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> store = new ConcurrentHashMap<>();

    public InMemoryCustomerRepository() {
        seed();
    }

    @Override
    public List<Customer> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Customer save(Customer customer) {
        store.put(customer.id(), customer);
        return customer;
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return store.values().stream().anyMatch(c -> c.email().equalsIgnoreCase(email));
    }

    private void seed() {
        Instant now = Instant.now();
        save(new Customer("cust-1001", "Ada Lovelace", "ada.lovelace@example.com",
                "123.456.789-00", "+55 11 90000-0001", now, now));
        save(new Customer("cust-1002", "Alan Turing", "alan.turing@example.com",
                "987.654.321-00", "+55 11 90000-0002", now, now));
    }
}
