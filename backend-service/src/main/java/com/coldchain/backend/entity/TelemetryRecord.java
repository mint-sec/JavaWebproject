package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record TelemetryRecord(
        String vehicleCode,
        LocalDateTime recordTime,
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
