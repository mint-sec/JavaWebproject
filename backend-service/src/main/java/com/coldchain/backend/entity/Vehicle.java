package com.coldchain.backend.entity;

public record Vehicle(
        Long id,
        String vehicleCode,
        String plateNumber,
        String cargoType,
        String cargoName,
        double safeTempMin,
        double safeTempMax,
        String status) {
}
