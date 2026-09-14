package com.marcosperboni.paymentservice.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcosperboni.paymentservice.application.PaymentService;
import com.marcosperboni.paymentservice.domain.Payment;
import com.marcosperboni.paymentservice.domain.PaymentMethod;
import com.marcosperboni.paymentservice.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void findAllFiltersByCustomerId() throws Exception {
        Instant now = Instant.now();
        Payment payment = new Payment("pay-1", "cust-1", "order-1", new BigDecimal("10.00"),
                PaymentMethod.PIX, PaymentStatus.PENDING, now, now);
        given(paymentService.findByCustomerId("cust-1")).willReturn(List.of(payment));

        mockMvc.perform(get("/api/payments").param("customerId", "cust-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("pay-1"));
    }

    @Test
    void createReturns201() throws Exception {
        Instant now = Instant.now();
        Payment created = new Payment("pay-2", "cust-1", "order-1", new BigDecimal("10.00"),
                PaymentMethod.PIX, PaymentStatus.PENDING, now, now);
        given(paymentService.create(eq("cust-1"), eq("order-1"), eq(new BigDecimal("10.00")), eq(PaymentMethod.PIX)))
                .willReturn(created);

        String body = objectMapper.writeValueAsString(new com.marcosperboni.paymentservice.web.dto.PaymentRequest(
                "cust-1", "order-1", new BigDecimal("10.00"), PaymentMethod.PIX, null));

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pay-2"));
    }

    @Test
    void createRejectsMissingMethod() throws Exception {
        String body = objectMapper.writeValueAsString(new com.marcosperboni.paymentservice.web.dto.PaymentRequest(
                "cust-1", "order-1", new BigDecimal("10.00"), null, null));

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateChangesStatus() throws Exception {
        Instant now = Instant.now();
        Payment updated = new Payment("pay-1", "cust-1", "order-1", new BigDecimal("10.00"),
                PaymentMethod.PIX, PaymentStatus.APPROVED, now, now);
        given(paymentService.update(eq("pay-1"), any(), any(), eq(PaymentStatus.APPROVED))).willReturn(updated);

        String body = objectMapper.writeValueAsString(new com.marcosperboni.paymentservice.web.dto.PaymentRequest(
                "cust-1", "order-1", new BigDecimal("10.00"), PaymentMethod.PIX, PaymentStatus.APPROVED));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/payments/pay-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/payments/pay-1")).andExpect(status().isNoContent());
    }
}
