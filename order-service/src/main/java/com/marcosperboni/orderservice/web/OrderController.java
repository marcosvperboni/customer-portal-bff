package com.marcosperboni.orderservice.web;

import com.marcosperboni.orderservice.application.OrderService;
import com.marcosperboni.orderservice.domain.Order;
import com.marcosperboni.orderservice.domain.OrderStatus;
import com.marcosperboni.orderservice.web.dto.OrderRequest;
import com.marcosperboni.orderservice.web.dto.OrderResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderResponse> findAll(@RequestParam(required = false) String customerId) {
        List<Order> orders = customerId == null ? service.findAll() : service.findByCustomerId(customerId);
        return orders.stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable String id) {
        return OrderResponse.from(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        Order created = service.create(request.customerId(), request.description(), request.amount());
        return ResponseEntity.created(URI.create("/api/orders/" + created.id()))
                .body(OrderResponse.from(created));
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable String id, @Valid @RequestBody OrderRequest request) {
        OrderStatus status = request.status() == null ? service.findById(id).status() : request.status();
        Order updated = service.update(id, request.description(), request.amount(), status);
        return OrderResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
