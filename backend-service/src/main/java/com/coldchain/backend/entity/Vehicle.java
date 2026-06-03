package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record Vehicle(
        Long id,
        String vehicleCode,
        String plateNumber,
        String cargoType,
        String cargoName,
        double safeTempMin,
        double safeTempMax,
        String status,
        String driver,
        String route,
        LocalDateTime updatedAt) {
}
