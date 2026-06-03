package com.coldchain.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminAlertUpdateRequest(
        @NotBlank String owner,
        @NotBlank String level,
        @NotBlank String status,
        String note) {
}
