package com.coldchain.backend.service;

import com.coldchain.backend.config.DataSourceModeProperties;
import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import com.coldchain.backend.entity.mysql.TelemetryRecordEntity;
import com.coldchain.backend.repository.MockDataRepository;
import com.coldchain.backend.repository.mysql.TelemetryRecordJpaRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RealtimeTelemetryService {
    private final DataSourceModeProperties dataSourceModeProperties;
    private final MockDataRepository mockDataRepository;
    private final SimulationTimelineService simulationTimelineService;
    private final VehicleSimulationService vehicleSimulationService;
    private final Optional<TelemetryRecordJpaRepository> telemetryRecordJpaRepository;
    private final long sampleIntervalSeconds;

    public RealtimeTelemetryService(
            DataSourceModeProperties dataSourceModeProperties,
            MockDataRepository mockDataRepository,
            SimulationTimelineService simulationTimelineService,
            VehicleSimulationService vehicleSimulationService,
            Optional<TelemetryRecordJpaRepository> telemetryRecordJpaRepository,
            @org.springframework.beans.factory.annotation.Value("${app.simulation.sample-interval-seconds:30}") long sampleIntervalSeconds) {
        this.dataSourceModeProperties = dataSourceModeProperties;
        this.mockDataRepository = mockDataRepository;
        this.simulationTimelineService = simulationTimelineService;
        this.vehicleSimulationService = vehicleSimulationService;
        this.telemetryRecordJpaRepository = telemetryRecordJpaRepository;
        this.sampleIntervalSeconds = Math.max(sampleIntervalSeconds, 5);
    }

    public TelemetryRecord getCurrentTelemetry(Vehicle vehicle) {
        if (!dataSourceModeProperties.useMysql()) {
            return simulationTimelineService.getCurrentTelemetry(vehicle.vehicleCode());
        }

        TelemetryRecord synthetic = vehicleSimulationService.buildCurrentTelemetry(vehicle);
        persistSampleIfDue(synthetic);
        return synthetic;
    }

    public List<TelemetryRecord> getTelemetryHistory(Vehicle vehicle, int minutes) {
        if (!dataSourceModeProperties.useMysql()) {
            return simulationTimelineService.getCurrentTelemetryHistory(vehicle.vehicleCode(), minutes);
        }

        TelemetryRecord current = getCurrentTelemetry(vehicle);
        List<TelemetryRecord> history = new ArrayList<>(mockDataRepository.findTelemetryHistoryByVehicleCode(vehicle.vehicleCode()));
        if (history.isEmpty()) {
            history.add(current);
        } else {
            TelemetryRecord last = history.get(history.size() - 1);
            if (current.recordTime().isAfter(last.recordTime())) {
                history.add(current);
            } else if (current.recordTime().isEqual(last.recordTime())) {
                history.set(history.size() - 1, current);
            }
        }

        if (minutes <= 0 || history.size() <= 1) {
            return history;
        }

        LocalDateTime boundary = history.get(history.size() - 1).recordTime().minusMinutes(minutes);
        List<TelemetryRecord> filtered = history.stream()
                .filter(record -> !record.recordTime().isBefore(boundary))
                .sorted(Comparator.comparing(TelemetryRecord::recordTime))
                .toList();
        return filtered.isEmpty() ? history : filtered;
    }

    public List<AlertRecord> buildLiveAlerts(Vehicle vehicle, TelemetryRecord telemetryRecord) {
        List<AlertRecord> alerts = new ArrayList<>();
        LocalDateTime triggerTime = telemetryRecord.recordTime();
        String owner = resolveOwnerName(vehicle.ownerUserId());

        if (telemetryRecord.temperature() >= vehicle.safeTempMax()) {
            alerts.add(new AlertRecord(
                    "LIVE-" + vehicle.vehicleCode() + "-TEMP-HIGH",
                    vehicle.vehicleCode(),
                    "HIGH",
                    "TEMP_LIMIT",
                    "温度已超安全上限",
                    "当前车厢温度已经达到或超过安全上限，存在较高货损风险。",
                    "建议立即检查制冷状态，并优先完成近端配送或改道冷库。",
                    triggerTime,
                    "OPEN",
                    owner,
                    vehicle.ownerUserId(),
                    "待处理",
                    "",
                    null,
                    "BUSINESS"));
        } else if (telemetryRecord.temperature() >= vehicle.safeTempMax() - 0.5) {
            alerts.add(new AlertRecord(
                    "LIVE-" + vehicle.vehicleCode() + "-TEMP-WARN",
                    vehicle.vehicleCode(),
                    "MEDIUM",
                    "TREND_WARNING",
                    "温度接近上限",
                    "当前温度接近安全上限，如果趋势继续，可能进入超温状态。",
                    "建议重点关注制冷效率和剩余路程。",
                    triggerTime,
                    "OPEN",
                    owner,
                    vehicle.ownerUserId(),
                    "待处理",
                    "",
                    null,
                    "BUSINESS"));
        }

        if (telemetryRecord.doorOpen()) {
            alerts.add(new AlertRecord(
                    "LIVE-" + vehicle.vehicleCode() + "-DOOR",
                    vehicle.vehicleCode(),
                    "MEDIUM",
                    "DOOR_EVENT",
                    "车门开启波动",
                    "车门处于开启状态，可能导致短时温升和湿度波动。",
                    "建议尽快完成装卸，并恢复制冷与关门状态。",
                    triggerTime,
                    "OPEN",
                    owner,
                    vehicle.ownerUserId(),
                    "处理中",
                    "",
                    null,
                    "BUSINESS"));
        }

        if (alerts.isEmpty()) {
            boolean completed = telemetryRecord.remainingKm() <= 0;
            alerts.add(new AlertRecord(
                    "LIVE-" + vehicle.vehicleCode() + "-NORMAL",
                    vehicle.vehicleCode(),
                    "LOW",
                    "NORMAL_STATUS",
                    completed ? "运输已完成" : "运输状态正常",
                    completed ? "当前车辆已完成本次运输任务。" : "当前温度、速度和位置状态正常。",
                    completed ? "可进入待命或下一任务准备。" : "继续按当前路线稳定运行。",
                    triggerTime,
                    "OPEN",
                    owner,
                    vehicle.ownerUserId(),
                    "待处理",
                    "",
                    null,
                    "BUSINESS"));
        }

        return alerts;
    }

    private void persistSampleIfDue(TelemetryRecord telemetryRecord) {
        TelemetryRecordJpaRepository repository = telemetryRecordJpaRepository.orElse(null);
        if (repository == null) {
            return;
        }

        Optional<TelemetryRecordEntity> latestOptional = repository.findFirstByVehicleCodeOrderByRecordTimeDesc(telemetryRecord.vehicleCode());
        if (latestOptional.isPresent()) {
            LocalDateTime latestRecordTime = latestOptional.get().getRecordTime();
            long seconds = Duration.between(latestRecordTime, telemetryRecord.recordTime()).getSeconds();
            if (seconds < sampleIntervalSeconds) {
                return;
            }
        }

        TelemetryRecordEntity entity = new TelemetryRecordEntity();
        entity.setVehicleCode(telemetryRecord.vehicleCode());
        entity.setRecordTime(telemetryRecord.recordTime());
        entity.setTemperature(telemetryRecord.temperature());
        entity.setHumidity(telemetryRecord.humidity());
        entity.setDoorOpen(telemetryRecord.doorOpen());
        entity.setSpeed(telemetryRecord.speed());
        entity.setOutsideTemp(telemetryRecord.outsideTemp());
        entity.setLng(telemetryRecord.lng());
        entity.setLat(telemetryRecord.lat());
        entity.setRemainingKm(telemetryRecord.remainingKm());
        entity.setTrend(telemetryRecord.trend());
        repository.save(entity);
    }

    private String resolveOwnerName(String ownerUserId) {
        return ownerUserId == null || ownerUserId.isBlank() ? "用户" : "用户";
    }
}
