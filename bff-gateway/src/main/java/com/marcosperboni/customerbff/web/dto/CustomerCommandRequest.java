package com.marcosperboni.customerbff.web.dto;

import com.marcosperboni.customerbff.domain.model.CustomerSummary;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerCommandRequest(
        @NotBlank(message = "name is required") @Size(max = 120) String name,
        @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
        @NotBlank(message = "document is required") String document,
        @NotBlank(message = "phone is required") String phone) {

    public CustomerSummary toSummary(String id) {
        return new CustomerSummary(id, name, email, document, phone);
    }
}
