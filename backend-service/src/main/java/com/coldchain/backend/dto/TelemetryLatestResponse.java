package com.coldchain.backend.dto;

public record TelemetryLatestResponse(
        String vehicleCode,
        String recordTime,
        double temperature,
        double humidity,
        boolean doorOpen,
        double speed,
        double outsideTemp,
        double lng,
        double lat,
        double remainingKm,
        String trend) {
}
