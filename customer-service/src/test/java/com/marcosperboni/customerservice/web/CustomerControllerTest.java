package com.marcosperboni.customerservice.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcosperboni.customerservice.application.CustomerService;
import com.marcosperboni.customerservice.domain.Customer;
import com.marcosperboni.customerservice.domain.CustomerNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void getByIdReturnsCustomer() throws Exception {
        Instant now = Instant.now();
        Customer customer = new Customer("cust-1", "Ada Lovelace", "ada@example.com", "doc", "phone", now, now);
        given(customerService.findById("cust-1")).willReturn(customer);

        mockMvc.perform(get("/api/customers/cust-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cust-1"))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        given(customerService.findById("missing")).willThrow(new CustomerNotFoundException("missing"));

        mockMvc.perform(get("/api/customers/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        Instant now = Instant.now();
        Customer created = new Customer("cust-2", "Grace Hopper", "grace@example.com", "doc", "phone", now, now);
        given(customerService.create(eq("Grace Hopper"), eq("grace@example.com"), eq("doc"), eq("phone")))
                .willReturn(created);

        String body = objectMapper.writeValueAsString(
                new com.marcosperboni.customerservice.web.dto.CustomerRequest("Grace Hopper", "grace@example.com", "doc", "phone"));

        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cust-2"));
    }

    @Test
    void createRejectsInvalidPayload() throws Exception {
        String body = objectMapper.writeValueAsString(
                new com.marcosperboni.customerservice.web.dto.CustomerRequest("", "not-an-email", "", ""));

        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void updateReturnsUpdatedCustomer() throws Exception {
        Instant now = Instant.now();
        Customer updated = new Customer("cust-1", "Ada L.", "ada.l@example.com", "doc", "phone", now, now);
        given(customerService.update(eq("cust-1"), any(), any(), any(), any())).willReturn(updated);

        String body = objectMapper.writeValueAsString(
                new com.marcosperboni.customerservice.web.dto.CustomerRequest("Ada L.", "ada.l@example.com", "doc", "phone"));

        mockMvc.perform(put("/api/customers/cust-1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada L."));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/customers/cust-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        willThrow(new CustomerNotFoundException("missing")).given(customerService).delete("missing");

        mockMvc.perform(delete("/api/customers/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("missing")));
    }
}
