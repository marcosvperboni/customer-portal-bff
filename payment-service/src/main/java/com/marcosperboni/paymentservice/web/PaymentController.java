package com.marcosperboni.paymentservice.web;

import com.marcosperboni.paymentservice.application.PaymentService;
import com.marcosperboni.paymentservice.domain.Payment;
import com.marcosperboni.paymentservice.domain.PaymentStatus;
import com.marcosperboni.paymentservice.web.dto.PaymentRequest;
import com.marcosperboni.paymentservice.web.dto.PaymentResponse;
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
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentResponse> findAll(@RequestParam(required = false) String customerId) {
        List<Payment> payments = customerId == null ? service.findAll() : service.findByCustomerId(customerId);
        return payments.stream().map(PaymentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PaymentResponse findById(@PathVariable String id) {
        return PaymentResponse.from(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        Payment created = service.create(request.customerId(), request.orderId(), request.amount(), request.method());
        return ResponseEntity.created(URI.create("/api/payments/" + created.id()))
                .body(PaymentResponse.from(created));
    }

    @PutMapping("/{id}")
    public PaymentResponse update(@PathVariable String id, @Valid @RequestBody PaymentRequest request) {
        PaymentStatus status = request.status() == null ? service.findById(id).status() : request.status();
        Payment updated = service.update(id, request.amount(), request.method(), status);
        return PaymentResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
