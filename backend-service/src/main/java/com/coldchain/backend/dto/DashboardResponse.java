package com.coldchain.backend.dto;

import java.util.List;

public record DashboardResponse(
        String vehicleCode,
        String scene,
        String systemTime,
        SafeRange safeRange,
        Summary summary,
        Route route,
        RiskAssessmentResponse latestRiskAssessment,
        List<TelemetryPointResponse> temperatureHistory,
        List<AlertResponse> alerts) {
    public record SafeRange(double min, double max) {
    }

    public record Summary(
            double temperature,
            double humidity,
            double speed,
            boolean doorOpen,
            String riskLevel,
            String riskLabel,
            double remainingKm,
            String eta,
            String trend) {
    }

    public record Route(
            String status,
            CurrentPosition currentPosition,
            List<Point> pathPoints,
            List<Destination> destinations,
            List<RoutePlanResponse> recommendations) {
    }

    public record CurrentPosition(double lng, double lat) {
    }

    public record Point(double lng, double lat) {
    }

    public record Destination(String name, double lng, double lat, String type) {
    }
}
