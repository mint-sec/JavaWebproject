package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RealtimeSimulationService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 6, 10, 9, 0, 0);

    private final long bootMillis = System.currentTimeMillis();
    private final Map<String, VehicleScenario> scenarios = buildScenarios();

    public EvaluateRequest.TelemetryPayload getCurrentTelemetry(String vehicleCode) {
        VehicleScenario scenario = scenarios.get(vehicleCode);
        if (scenario == null) {
            return null;
        }

        double elapsedSeconds = Math.max(0, (System.currentTimeMillis() - bootMillis) / 1000.0);
        double progress = Math.min(elapsedSeconds / scenario.totalDurationSeconds(), 1.0);
        return buildTelemetry(scenario, progress);
    }

    public Map<String, EvaluateRequest.TelemetryPayload> getAllCurrentTelemetry() {
        Map<String, EvaluateRequest.TelemetryPayload> result = new LinkedHashMap<>();
        for (String vehicleCode : scenarios.keySet()) {
            result.put(vehicleCode, getCurrentTelemetry(vehicleCode));
        }
        return result;
    }

    private EvaluateRequest.TelemetryPayload buildTelemetry(VehicleScenario scenario, double progress) {
        LocalDateTime recordTime = BASE_TIME.plus(Duration.ofSeconds(Math.round(progress * scenario.totalDurationSeconds())));
        double lng = interpolate(scenario.startLng(), scenario.endLng(), progress);
        double lat = interpolate(scenario.startLat(), scenario.endLat(), progress);
        double remainingKm = round1(scenario.totalKm() * (1.0 - progress));
        boolean completed = remainingKm <= 0.0;

        double temperature = scenario.risky()
                ? riskyTemperature(progress)
                : normalTemperature(scenario.baseTemp(), progress);
        boolean doorOpen = isDoorOpen(scenario.risky(), progress, completed);
        double speed = completed ? 0.0 : computeSpeed(scenario.risky(), progress, doorOpen);
        double humidity = computeHumidity(temperature, progress);
        double outsideTemp = round1(27.0 + progress * 8.0 + Math.sin(progress * Math.PI * 3.0) * 1.6);
        String trend = buildTrendLabel(temperature, scenario.safeTempMax(), completed);

        EvaluateRequest.TelemetryPayload payload = new EvaluateRequest.TelemetryPayload();
        payload.setRecordTime(recordTime.format(FULL_TIME));
        payload.setTemperature(round1(temperature));
        payload.setHumidity(round1(humidity));
        payload.setDoorOpen(doorOpen);
        payload.setSpeed(round1(speed));
        payload.setOutsideTemp(outsideTemp);
        payload.setLng(round3(lng));
        payload.setLat(round3(lat));
        payload.setRemainingKm(completed ? 0.0 : remainingKm);
        payload.setTrend(trend);
        return payload;
    }

    private double riskyTemperature(double progress) {
        double value;
        if (progress < 0.25) {
            value = 4.5 + progress * 3.2;
        } else if (progress < 0.55) {
            value = 5.3 + (progress - 0.25) * 5.2;
        } else if (progress < 0.82) {
            value = 6.9 + (progress - 0.55) * 7.8;
        } else {
            value = 8.4 - (progress - 0.82) * 7.0;
        }
        return value + Math.sin(progress * Math.PI * 7.0) * 0.18;
    }

    private double normalTemperature(double baseTemp, double progress) {
        return baseTemp + Math.sin(progress * Math.PI * 4.0) * 0.35 + Math.cos(progress * Math.PI * 1.5) * 0.12;
    }

    private boolean isDoorOpen(boolean risky, double progress, boolean completed) {
        if (completed) {
            return false;
        }
        if (risky) {
            return inRange(progress, 0.14, 0.17)
                    || inRange(progress, 0.42, 0.45)
                    || inRange(progress, 0.68, 0.71);
        }
        return inRange(progress, 0.50, 0.52);
    }

    private double computeSpeed(boolean risky, double progress, boolean doorOpen) {
        if (doorOpen) {
            return 0.0;
        }
        double base = risky ? 39.0 : 43.0;
        return base + Math.sin(progress * Math.PI * 5.0) * 8.0 + Math.cos(progress * Math.PI * 3.0) * 4.0;
    }

    private double computeHumidity(double temperature, double progress) {
        return 63.0 + (temperature - 4.0) * 1.7 + Math.cos(progress * Math.PI * 4.5) * 2.2;
    }

    private String buildTrendLabel(double temperature, double safeTempMax, boolean completed) {
        if (completed) {
            return "运输完成";
        }
        if (temperature >= safeTempMax + 0.5) {
            return "严重超标";
        }
        if (temperature >= safeTempMax) {
            return "温度超标";
        }
        if (temperature >= safeTempMax - 0.5) {
            return "接近上限";
        }
        if (temperature >= safeTempMax - 1.5) {
            return "趋势预警";
        }
        if (temperature >= 5.0) {
            return "缓慢升温";
        }
        return "温度平稳";
    }

    private boolean inRange(double value, double start, double end) {
        return value >= start && value <= end;
    }

    private double interpolate(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private Map<String, VehicleScenario> buildScenarios() {
        Map<String, VehicleScenario> map = new LinkedHashMap<>();
        map.put("CC-VA-01", new VehicleScenario("CC-VA-01", 116.360, 39.900, 116.850, 39.940, 50.0, 4.8, 8.0, true, 48 * 60));
        map.put("CC-VA-02", new VehicleScenario("CC-VA-02", 116.390, 39.906, 116.720, 39.980, 28.0, 5.0, 8.0, false, 36 * 60));
        map.put("CC-VA-03", new VehicleScenario("CC-VA-03", 116.398, 39.902, 116.750, 39.955, 24.0, 5.8, 8.0, false, 34 * 60));
        map.put("CC-VA-04", new VehicleScenario("CC-VA-04", 116.408, 39.907, 116.770, 39.985, 31.0, 4.7, 8.0, false, 40 * 60));
        map.put("CC-VA-05", new VehicleScenario("CC-VA-05", 116.418, 39.913, 116.790, 39.992, 26.0, 6.2, 8.0, true, 38 * 60));
        return map;
    }

    public record VehicleScenario(
            String vehicleCode,
            double startLng,
            double startLat,
            double endLng,
            double endLat,
            double totalKm,
            double baseTemp,
            double safeTempMax,
            boolean risky,
            long totalDurationSeconds) {
    }
}
