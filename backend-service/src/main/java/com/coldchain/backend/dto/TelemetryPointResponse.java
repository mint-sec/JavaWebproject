package com.coldchain.backend.dto;

public record TelemetryPointResponse(
        String time,
        String recordTime,
        double temperature) {
}
