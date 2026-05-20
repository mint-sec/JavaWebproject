package com.coldchain.backend.dto;

public record VehicleResponse(
        String vehicleCode,
        String plateNumber,
        String cargoType,
        String cargoName,
        double safeTempMin,
        double safeTempMax,
        String status) {
}
