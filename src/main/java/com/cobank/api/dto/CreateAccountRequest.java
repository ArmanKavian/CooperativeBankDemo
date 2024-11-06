package com.cobank.api.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Address is required")
        String address,

        @Email(message = "Invalid email format")
        String email
) {
}

