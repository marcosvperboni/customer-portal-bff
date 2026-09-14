package com.marcosperboni.customerservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marcosperboni.customerservice.domain.Customer;
import com.marcosperboni.customerservice.domain.CustomerNotFoundException;
import com.marcosperboni.customerservice.infrastructure.InMemoryCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(new InMemoryCustomerRepository());
    }

    @Test
    void createsCustomerWithGeneratedId() {
        Customer created = service.create("Grace Hopper", "grace@example.com", "111.222.333-44", "+55 11 91111-1111");

        assertThat(created.id()).startsWith("cust-");
        assertThat(service.findById(created.id())).isEqualTo(created);
    }

    @Test
    void rejectsDuplicateEmailOnCreate() {
        service.create("Grace Hopper", "grace@example.com", "111.222.333-44", "+55 11 91111-1111");

        assertThatThrownBy(() -> service.create("Duplicate", "grace@example.com", "000", "000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grace@example.com");
    }

    @Test
    void throwsNotFoundForUnknownCustomer() {
        assertThatThrownBy(() -> service.findById("does-not-exist"))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void updatesExistingCustomerFields() {
        Customer created = service.create("Grace Hopper", "grace@example.com", "111", "222");

        Customer updated = service.update(created.id(), "Grace M. Hopper", "grace.hopper@example.com", "111", "333");

        assertThat(updated.name()).isEqualTo("Grace M. Hopper");
        assertThat(updated.phone()).isEqualTo("333");
        assertThat(updated.updatedAt()).isAfterOrEqualTo(created.updatedAt());
    }

    @Test
    void deletesExistingCustomer() {
        Customer created = service.create("Grace Hopper", "grace@example.com", "111", "222");

        service.delete(created.id());

        assertThatThrownBy(() -> service.findById(created.id())).isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void throwsNotFoundWhenDeletingUnknownCustomer() {
        assertThatThrownBy(() -> service.delete("does-not-exist")).isInstanceOf(CustomerNotFoundException.class);
    }
}
