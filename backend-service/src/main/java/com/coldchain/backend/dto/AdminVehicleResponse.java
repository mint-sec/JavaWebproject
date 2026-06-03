package com.coldchain.backend.dto;

public record AdminVehicleResponse(
        String vehicleId,
        String cargoName,
        String status,
        String driver,
        String route,
        String updatedAt) {
}
