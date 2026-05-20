package com.coldchain.backend.service;

import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.RiskAssessmentRecord;
import com.coldchain.backend.entity.RoutePlanRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.exception.NotFoundException;
import com.coldchain.backend.repository.MockDataRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SimulationTimelineService {
    private final MockDataRepository repository;
    private final Instant bootTime;
    private final long stepSeconds;

    public SimulationTimelineService(
            MockDataRepository repository,
            @Value("${app.simulation.step-seconds:5}") long stepSeconds) {
        this.repository = repository;
        this.stepSeconds = Math.max(stepSeconds, 1);
        this.bootTime = Instant.now();
    }

    public TelemetryRecord getCurrentTelemetry(String vehicleCode) {
        List<TelemetryRecord> timeline = repository.findTelemetryHistoryByVehicleCode(vehicleCode);
        if (timeline.isEmpty()) {
            throw new NotFoundException("未找到车辆历史温度数据: " + vehicleCode);
        }
        return timeline.get(resolveCurrentIndex(timeline.size()));
    }

    public List<TelemetryRecord> getCurrentTelemetryHistory(String vehicleCode, int minutes) {
        List<TelemetryRecord> timeline = repository.findTelemetryHistoryByVehicleCode(vehicleCode);
        if (timeline.isEmpty()) {
            throw new NotFoundException("未找到车辆历史温度数据: " + vehicleCode);
        }

        int currentIndex = resolveCurrentIndex(timeline.size());
        List<TelemetryRecord> visible = timeline.subList(0, currentIndex + 1);
        if (minutes <= 0 || visible.size() <= 1) {
            return visible;
        }

        LocalDateTime boundary = visible.get(visible.size() - 1).recordTime().minusMinutes(minutes);
        List<TelemetryRecord> filtered = visible.stream()
                .filter(record -> !record.recordTime().isBefore(boundary))
                .toList();
        return filtered.isEmpty() ? visible : filtered;
    }

    public List<AlertRecord> getCurrentAlerts(String vehicleCode) {
        LocalDateTime currentTime = getCurrentTelemetry(vehicleCode).recordTime();
        return repository.findAlertsByVehicleCode(vehicleCode).stream()
                .filter(alert -> !alert.triggerTime().isAfter(currentTime))
                .sorted(Comparator.comparing(AlertRecord::triggerTime).reversed())
                .toList();
    }

    public List<RiskAssessmentRecord> getCurrentRiskAssessments(String vehicleCode) {
        LocalDateTime currentTime = getCurrentTelemetry(vehicleCode).recordTime();
        return repository.findRiskAssessmentsByVehicleCode(vehicleCode).stream()
                .filter(record -> !record.assessmentTime().isAfter(currentTime))
                .sorted(Comparator.comparing(RiskAssessmentRecord::assessmentTime).reversed())
                .toList();
    }

    public List<RoutePlanRecord> getCurrentRoutePlans(String vehicleCode) {
        LocalDateTime currentTime = getCurrentTelemetry(vehicleCode).recordTime();
        return repository.findRoutePlansByVehicleCode(vehicleCode).stream()
                .filter(record -> !record.createdTime().isAfter(currentTime))
                .sorted(Comparator.comparing(RoutePlanRecord::createdTime).reversed())
                .toList();
    }

    private int resolveCurrentIndex(int size) {
        if (size <= 1) {
            return 0;
        }
        long elapsedSeconds = Duration.between(bootTime, Instant.now()).getSeconds();
        long elapsedSteps = Math.max(elapsedSeconds / stepSeconds, 0);
        return (int) Math.min(elapsedSteps, size - 1);
    }
}
