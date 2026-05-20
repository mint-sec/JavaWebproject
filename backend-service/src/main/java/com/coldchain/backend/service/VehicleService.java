package com.coldchain.backend.service;

import com.coldchain.backend.dto.AlertResponse;
import com.coldchain.backend.dto.TelemetryLatestResponse;
import com.coldchain.backend.dto.TelemetryPointResponse;
import com.coldchain.backend.dto.VehicleResponse;
import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import com.coldchain.backend.exception.NotFoundException;
import com.coldchain.backend.repository.MockDataRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final MockDataRepository repository;

    public VehicleService(MockDataRepository repository) {
        this.repository = repository;
    }

    public List<VehicleResponse> getVehicles() {
        return repository.findAllVehicles().stream().map(this::toVehicleResponse).toList();
    }

    public TelemetryLatestResponse getLatestTelemetry(String vehicleCode) {
        TelemetryRecord record = repository.findLatestTelemetryByVehicleCode(vehicleCode)
                .orElseThrow(() -> new NotFoundException("未找到车辆最新温度数据: " + vehicleCode));
        return toLatestResponse(record);
    }

    public List<TelemetryPointResponse> getTelemetryHistory(String vehicleCode, int minutes) {
        validateVehicle(vehicleCode);
        return repository.findTelemetryHistoryByVehicleCode(vehicleCode).stream()
                .map(this::toTelemetryPoint)
                .toList();
    }

    public List<AlertResponse> getVehicleAlerts(String vehicleCode, Integer limit) {
        validateVehicle(vehicleCode);
        List<AlertResponse> results = repository.findAlertsByVehicleCode(vehicleCode).stream()
                .map(this::toAlertResponse)
                .toList();
        if (limit == null || limit <= 0 || limit >= results.size()) {
            return results;
        }
        return results.subList(0, limit);
    }

    public List<AlertResponse> getAlerts(String vehicleCode, int page, int pageSize) {
        List<AlertResponse> alerts = getVehicleAlerts(vehicleCode, null);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = (safePage - 1) * safePageSize;
        if (fromIndex >= alerts.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + safePageSize, alerts.size());
        return alerts.subList(fromIndex, toIndex);
    }

    public AlertResponse getAlertDetail(String alertId) {
        AlertRecord alert = repository.findAlertById(alertId)
                .orElseThrow(() -> new NotFoundException("未找到告警记录: " + alertId));
        return toAlertResponse(alert);
    }

    public Vehicle getVehicleEntity(String vehicleCode) {
        return validateVehicle(vehicleCode);
    }

    public List<TelemetryRecord> getTelemetryEntities(String vehicleCode) {
        validateVehicle(vehicleCode);
        return repository.findTelemetryHistoryByVehicleCode(vehicleCode);
    }

    private Vehicle validateVehicle(String vehicleCode) {
        return repository.findVehicleByCode(vehicleCode)
                .orElseThrow(() -> new NotFoundException("未找到车辆信息: " + vehicleCode));
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.vehicleCode(),
                vehicle.plateNumber(),
                vehicle.cargoType(),
                vehicle.cargoName(),
                vehicle.safeTempMin(),
                vehicle.safeTempMax(),
                vehicle.status());
    }

    private TelemetryLatestResponse toLatestResponse(TelemetryRecord record) {
        return new TelemetryLatestResponse(
                record.vehicleCode(),
                record.recordTime().format(FULL_TIME),
                record.temperature(),
                record.humidity(),
                record.doorOpen(),
                record.speed(),
                record.outsideTemp(),
                record.lng(),
                record.lat(),
                record.remainingKm(),
                record.trend());
    }

    private TelemetryPointResponse toTelemetryPoint(TelemetryRecord record) {
        return new TelemetryPointResponse(
                record.recordTime().format(SHORT_TIME),
                record.recordTime().format(FULL_TIME),
                record.temperature());
    }

    private AlertResponse toAlertResponse(AlertRecord alert) {
        return new AlertResponse(
                alert.alertId(),
                alert.vehicleCode(),
                alert.level(),
                alert.title(),
                alert.detail(),
                alert.suggestion(),
                alert.triggerTime().format(FULL_TIME));
    }
}
