package com.coldchain.backend.dto;

public record WorkspaceVehicleResponse(
        String vehicleKey,
        String vehicleId,
        String cargoName,
        String status,
        String driver,
        String route,
        double routeDistanceKm,
        String ownerUserId,
        String ownerName,
        String updatedAt) {
}
