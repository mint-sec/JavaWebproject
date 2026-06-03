package com.coldchain.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String phone,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotBlank String confirmPassword) {
}
