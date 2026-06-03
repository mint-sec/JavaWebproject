package com.coldchain.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String role,
        @NotBlank String status) {
}
