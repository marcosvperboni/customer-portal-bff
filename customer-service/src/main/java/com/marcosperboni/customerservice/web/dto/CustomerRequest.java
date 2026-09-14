package com.marcosperboni.customerservice.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "name is required") @Size(max = 120) String name,
        @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
        @NotBlank(message = "document is required") String document,
        @NotBlank(message = "phone is required") String phone) {
}
