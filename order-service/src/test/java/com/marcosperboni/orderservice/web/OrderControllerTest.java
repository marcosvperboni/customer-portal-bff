package com.marcosperboni.orderservice.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcosperboni.orderservice.application.OrderService;
import com.marcosperboni.orderservice.domain.Order;
import com.marcosperboni.orderservice.domain.OrderStatus;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void findAllFiltersByCustomerId() throws Exception {
        Instant now = Instant.now();
        Order order = new Order("order-1", "cust-1", "Item", new BigDecimal("10.00"), OrderStatus.CREATED, now, now);
        given(orderService.findByCustomerId("cust-1")).willReturn(List.of(order));

        mockMvc.perform(get("/api/orders").param("customerId", "cust-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("order-1"));
    }

    @Test
    void createReturns201() throws Exception {
        Instant now = Instant.now();
        Order created = new Order("order-2", "cust-1", "Item", new BigDecimal("10.00"), OrderStatus.CREATED, now, now);
        given(orderService.create(eq("cust-1"), eq("Item"), eq(new BigDecimal("10.00")))).willReturn(created);

        String body = objectMapper.writeValueAsString(
                new com.marcosperboni.orderservice.web.dto.OrderRequest("cust-1", "Item", new BigDecimal("10.00"), null));

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("order-2"));
    }

    @Test
    void createRejectsNonPositiveAmount() throws Exception {
        String body = objectMapper.writeValueAsString(
                new com.marcosperboni.orderservice.web.dto.OrderRequest("cust-1", "Item", new BigDecimal("0.00"), null));

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateChangesStatus() throws Exception {
        Instant now = Instant.now();
        Order updated = new Order("order-1", "cust-1", "Item", new BigDecimal("10.00"), OrderStatus.SHIPPED, now, now);
        given(orderService.update(eq("order-1"), any(), any(), eq(OrderStatus.SHIPPED))).willReturn(updated);

        String body = objectMapper.writeValueAsString(
                new com.marcosperboni.orderservice.web.dto.OrderRequest("cust-1", "Item", new BigDecimal("10.00"), OrderStatus.SHIPPED));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/orders/order-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/order-1")).andExpect(status().isNoContent());
    }
}
