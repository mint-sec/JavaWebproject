package com.coldchain.backend.service;

import com.coldchain.backend.dto.AlertResponse;
import com.coldchain.backend.dto.DashboardResponse;
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
    private static final DateTimeFormatter ETA_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final VehicleService vehicleService;

    public DashboardService(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    public DashboardResponse getVehicleDashboard(String vehicleCode) {
        Vehicle vehicle = vehicleService.getVehicleEntity(vehicleCode);
        List<TelemetryRecord> telemetry = vehicleService.getTelemetryEntities(vehicleCode);
        TelemetryRecord latest = telemetry.get(telemetry.size() - 1);
        List<TelemetryPointResponse> history = vehicleService.getTelemetryHistory(vehicleCode, 30);
        List<AlertResponse> alerts = vehicleService.getVehicleAlerts(vehicleCode, 4);

        DashboardResponse.Summary summary = new DashboardResponse.Summary(
                latest.temperature(),
                latest.humidity(),
                latest.speed(),
                latest.doorOpen(),
                "HIGH",
                "高风险",
                latest.remainingKm(),
                latest.recordTime().plusMinutes(46).format(ETA_TIME),
                latest.trend());

        DashboardResponse.Route route = new DashboardResponse.Route(
                "候选方案: 改道冷库",
                new DashboardResponse.CurrentPosition(latest.lng(), latest.lat()),
                List.of(
                        new DashboardResponse.Point(116.360, 39.900),
                        new DashboardResponse.Point(116.372, 39.901),
                        new DashboardResponse.Point(116.384, 39.903),
                        new DashboardResponse.Point(116.397, 39.908)),
                List.of(
                        new DashboardResponse.Destination("配送中心", 116.360, 39.900, "origin"),
                        new DashboardResponse.Destination("医院 A", 116.384, 39.903, "waypoint"),
                        new DashboardResponse.Destination("冷库 C1", 116.402, 39.910, "cold-storage")));

        return new DashboardResponse(
                vehicle.vehicleCode(),
                "疫苗冷链配送",
                LocalDateTime.now().format(FULL_TIME),
                new DashboardResponse.SafeRange(vehicle.safeTempMin(), vehicle.safeTempMax()),
                summary,
                route,
                history,
                alerts);
    }
}
