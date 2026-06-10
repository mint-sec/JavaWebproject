package com.coldchain.backend.service;

import com.coldchain.backend.dto.AlertResponse;
import com.coldchain.backend.dto.DashboardResponse;
import com.coldchain.backend.dto.RiskAssessmentResponse;
import com.coldchain.backend.dto.RoutePlanResponse;
import com.coldchain.backend.dto.TelemetryPointResponse;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final VehicleService vehicleService;
    private final AnalysisService analysisService;
    private final RealtimeTelemetryService realtimeTelemetryService;

    public DashboardService(
            VehicleService vehicleService,
            AnalysisService analysisService,
            RealtimeTelemetryService realtimeTelemetryService) {
        this.vehicleService = vehicleService;
        this.analysisService = analysisService;
        this.realtimeTelemetryService = realtimeTelemetryService;
    }

    public DashboardResponse getVehicleDashboard(String userId, String vehicleCode) {
        Vehicle vehicle = vehicleService.getOwnedVehicleEntity(userId, vehicleCode);
        List<TelemetryRecord> telemetry = realtimeTelemetryService.getTelemetryHistory(vehicle, 30);
        TelemetryRecord latest = realtimeTelemetryService.getCurrentTelemetry(vehicle);
        List<TelemetryPointResponse> history = telemetry.stream().map(this::toTelemetryPoint).toList();
        List<AlertResponse> alerts = vehicleService.getVehicleAlerts(userId, vehicleCode, 4);
        RiskAssessmentResponse risk = analysisService.getLatestRiskAssessment(vehicleCode);
        RoutePlanResponse latestRoutePlan = analysisService.getLatestRoutePlan(vehicleCode);
        List<RoutePlanResponse> routePlans = analysisService.getRoutePlans(vehicleCode, 2);

        DashboardResponse.Summary summary = new DashboardResponse.Summary(
                latest.temperature(),
                latest.humidity(),
                latest.speed(),
                latest.doorOpen(),
                risk.riskLevel(),
                risk.riskLabel(),
                latest.remainingKm(),
                estimateEta(latest).format(FULL_TIME),
                latest.trend());

        List<DashboardResponse.Point> pathPoints = telemetry.stream()
                .map(item -> new DashboardResponse.Point(item.lng(), item.lat()))
                .toList();

        DashboardResponse.Route route = new DashboardResponse.Route(
                latestRoutePlan.planTitle(),
                new DashboardResponse.CurrentPosition(latest.lng(), latest.lat()),
                pathPoints,
                buildDestinations(pathPoints),
                routePlans);

        return new DashboardResponse(
                vehicle.vehicleCode(),
                vehicle.cargoName() + "冷链运输",
                LocalDateTime.now().format(FULL_TIME),
                new DashboardResponse.SafeRange(vehicle.safeTempMin(), vehicle.safeTempMax()),
                summary,
                route,
                risk,
                history,
                alerts);
    }

    private List<DashboardResponse.Destination> buildDestinations(List<DashboardResponse.Point> pathPoints) {
        if (pathPoints.isEmpty()) {
            return List.of();
        }
        DashboardResponse.Point start = pathPoints.get(0);
        DashboardResponse.Point end = pathPoints.get(pathPoints.size() - 1);
        return List.of(
                new DashboardResponse.Destination("起点", start.lng(), start.lat(), "origin"),
                new DashboardResponse.Destination("当前位置", end.lng(), end.lat(), "waypoint"),
                new DashboardResponse.Destination("终点", end.lng(), end.lat(), "destination"));
    }

    private LocalDateTime estimateEta(TelemetryRecord latest) {
        if (latest.remainingKm() <= 0 || latest.speed() <= 0) {
            return latest.recordTime();
        }
        long minutes = Math.max(1L, Math.round((latest.remainingKm() / Math.max(latest.speed(), 1.0)) * 60.0));
        return latest.recordTime().plusMinutes(minutes);
    }

    private TelemetryPointResponse toTelemetryPoint(TelemetryRecord record) {
        return new TelemetryPointResponse(
                record.recordTime().format(SHORT_TIME),
                record.recordTime().format(FULL_TIME),
                record.temperature());
    }
}
