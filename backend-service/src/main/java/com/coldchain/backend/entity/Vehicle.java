package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record Vehicle(
        Long id,
        String vehicleCode,
        String displayCode,
        String plateNumber,
        String cargoType,
        String cargoName,
        double safeTempMin,
        double safeTempMax,
        String status,
        String ownerUserId,
        String driver,
        String route,
        double routeDistanceKm,
        LocalDateTime updatedAt) {
}
