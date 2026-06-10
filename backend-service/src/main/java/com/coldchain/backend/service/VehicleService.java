package com.coldchain.backend.service;

import com.coldchain.backend.dto.AlertResponse;
import com.coldchain.backend.dto.TelemetryLatestResponse;
import com.coldchain.backend.dto.TelemetryPointResponse;
import com.coldchain.backend.dto.VehicleResponse;
import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import com.coldchain.backend.exception.AuthException;
import com.coldchain.backend.exception.NotFoundException;
import com.coldchain.backend.repository.MockDataRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final MockDataRepository repository;
    private final RealtimeTelemetryService realtimeTelemetryService;

    public VehicleService(MockDataRepository repository, RealtimeTelemetryService realtimeTelemetryService) {
        this.repository = repository;
        this.realtimeTelemetryService = realtimeTelemetryService;
    }

    public List<VehicleResponse> getVehicles(String userId) {
        return repository.findVehiclesByOwnerUserId(userId).stream().map(this::toVehicleResponse).toList();
    }

    public List<Vehicle> getVehicleEntities() {
        return repository.findAllVehicles();
    }

    public TelemetryLatestResponse getLatestTelemetry(String userId, String vehicleCode) {
        Vehicle vehicle = validateOwnedVehicle(userId, vehicleCode);
        return toLatestResponse(vehicle, realtimeTelemetryService.getCurrentTelemetry(vehicle));
    }

    public List<TelemetryPointResponse> getTelemetryHistory(String userId, String vehicleCode, int minutes) {
        Vehicle vehicle = validateOwnedVehicle(userId, vehicleCode);
        return realtimeTelemetryService.getTelemetryHistory(vehicle, minutes).stream()
                .map(this::toTelemetryPoint)
                .toList();
    }

    public List<AlertResponse> getVehicleAlerts(String userId, String vehicleCode, Integer limit) {
        Vehicle vehicle = validateOwnedVehicle(userId, vehicleCode);
        List<AlertResponse> results = getLiveAlertEntities(vehicle).stream()
                .map(this::toAlertResponse)
                .toList();
        if (limit == null || limit <= 0 || limit >= results.size()) {
            return results;
        }
        return results.subList(0, limit);
    }

    public List<AlertResponse> getAlerts(String userId, String vehicleCode, int page, int pageSize) {
        List<AlertResponse> alerts = getVehicleAlerts(userId, vehicleCode, null);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = (safePage - 1) * safePageSize;
        if (fromIndex >= alerts.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + safePageSize, alerts.size());
        return alerts.subList(fromIndex, toIndex);
    }

    public AlertResponse getAlertDetail(String userId, String alertId) {
        AlertRecord alert = repository.findAlertById(alertId)
                .orElseThrow(() -> new NotFoundException("未找到告警记录: " + alertId));
        ensureVehicleOwnership(userId, alert.vehicleCode());
        return toAlertResponse(alert);
    }

    public Vehicle getVehicleEntity(String vehicleCode) {
        return repository.findVehicleByCode(vehicleCode)
                .orElseThrow(() -> new NotFoundException("未找到车辆信息: " + vehicleCode));
    }

    public Vehicle getOwnedVehicleEntity(String userId, String vehicleCode) {
        return validateOwnedVehicle(userId, vehicleCode);
    }

    public List<TelemetryRecord> getTelemetryEntities(String userId, String vehicleCode) {
        Vehicle vehicle = validateOwnedVehicle(userId, vehicleCode);
        return realtimeTelemetryService.getTelemetryHistory(vehicle, Integer.MAX_VALUE);
    }

    public List<TelemetryRecord> getTelemetryEntities(String vehicleCode) {
        Vehicle vehicle = getVehicleEntity(vehicleCode);
        return realtimeTelemetryService.getTelemetryHistory(vehicle, Integer.MAX_VALUE);
    }

    public List<AlertRecord> getLiveAlertEntities(String vehicleCode) {
        Vehicle vehicle = getVehicleEntity(vehicleCode);
        return getLiveAlertEntities(vehicle);
    }

    private List<AlertRecord> getLiveAlertEntities(Vehicle vehicle) {
        TelemetryRecord currentTelemetry = realtimeTelemetryService.getCurrentTelemetry(vehicle);
        return realtimeTelemetryService.buildLiveAlerts(vehicle, currentTelemetry);
    }

    private Vehicle validateOwnedVehicle(String userId, String vehicleCode) {
        Vehicle vehicle = repository.findVehicleByCode(vehicleCode)
                .orElseThrow(() -> new NotFoundException("未找到车辆信息: " + vehicleCode));
        if (!userId.equals(vehicle.ownerUserId())) {
            throw new AuthException(403, "当前账号无权访问该车辆");
        }
        return vehicle;
    }

    private void ensureVehicleOwnership(String userId, String vehicleCode) {
        validateOwnedVehicle(userId, vehicleCode);
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.vehicleCode(),
                vehicle.displayCode(),
                vehicle.vehicleCode(),
                vehicle.plateNumber(),
                vehicle.cargoType(),
                vehicle.cargoName(),
                vehicle.safeTempMin(),
                vehicle.safeTempMax(),
                vehicle.status(),
                vehicle.routeDistanceKm());
    }

    private TelemetryLatestResponse toLatestResponse(Vehicle vehicle, TelemetryRecord record) {
        return new TelemetryLatestResponse(
                vehicle.displayCode(),
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
        String displayCode = repository.findVehicleByCode(alert.vehicleCode())
                .map(Vehicle::displayCode)
                .orElse(alert.vehicleCode());
        return new AlertResponse(
                alert.alertId(),
                displayCode,
                alert.level(),
                alert.title(),
                alert.detail(),
                alert.suggestion(),
                alert.triggerTime().format(FULL_TIME));
    }
}
