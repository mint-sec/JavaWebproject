package com.coldchain.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminVehicleRequest(
        @NotBlank String vehicleId,
        @NotBlank String cargoName,
        @NotBlank String status,
        @NotBlank String driver,
        @NotBlank String route) {
}
