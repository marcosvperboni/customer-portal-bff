package com.marcosperboni.customerservice.web;

import com.marcosperboni.customerservice.application.CustomerService;
import com.marcosperboni.customerservice.domain.Customer;
import com.marcosperboni.customerservice.web.dto.CustomerRequest;
import com.marcosperboni.customerservice.web.dto.CustomerResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerResponse> findAll() {
        return service.findAll().stream().map(CustomerResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable String id) {
        return CustomerResponse.from(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer created = service.create(request.name(), request.email(), request.document(), request.phone());
        return ResponseEntity.created(URI.create("/api/customers/" + created.id()))
                .body(CustomerResponse.from(created));
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable String id, @Valid @RequestBody CustomerRequest request) {
        Customer updated = service.update(id, request.name(), request.email(), request.document(), request.phone());
        return CustomerResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
