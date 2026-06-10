package com.coldchain.backend.service;

import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class VehicleSimulationService {
    private static final double SIMULATION_AVERAGE_SPEED_KMH = 42.0;
    private static final long MIN_TIMESTEP_SECONDS = 1L;
    private static final long SIMULATION_STEP_SECONDS = 5L;

    private final Map<String, LocalDateTime> sessionStartTimes = new ConcurrentHashMap<>();

    public TelemetryRecord buildCurrentTelemetry(Vehicle vehicle) {
        double totalKm = Math.max(vehicle.routeDistanceKm(), 0.1);
        LocalDateTime startTime = sessionStartTimes.computeIfAbsent(vehicle.vehicleCode(), key -> LocalDateTime.now());
        long elapsedSeconds = Math.max(0L, Duration.between(startTime, LocalDateTime.now()).getSeconds());
        long expectedDurationSeconds = Math.max(
                MIN_TIMESTEP_SECONDS,
                Math.round((totalKm / SIMULATION_AVERAGE_SPEED_KMH) * 3600.0));

        long seed = Math.abs((vehicle.ownerUserId() + "|" + vehicle.displayCode()).hashCode());
        double startLng = 116.180 + (seed % 450) / 1000.0;
        double startLat = 39.720 + ((seed / 11) % 240) / 1000.0;
        double endLng = startLng + 0.18 + ((seed / 17) % 90) / 1000.0;
        double endLat = startLat + 0.04 + ((seed / 23) % 50) / 1000.0;

        boolean risky = seed % 5 == 0 || ("IN_TRANSIT".equals(vehicle.status()) && totalKm >= 35.0);
        TravelState travelState = simulateTravel(totalKm, elapsedSeconds, expectedDurationSeconds, risky);
        double progress = travelState.progress();
        double safeMid = (vehicle.safeTempMin() + vehicle.safeTempMax()) / 2.0;
        double temperature = risky ? riskyTemperature(safeMid, vehicle.safeTempMax(), progress) : stableTemperature(safeMid, progress);
        boolean completed = travelState.completed();
        boolean doorOpen = travelState.doorOpen();
        double speed = travelState.speedKmh();
        double remainingKm = travelState.remainingKm();
        double outsideTemp = round1(27.0 + progress * 7.0 + Math.sin(progress * Math.PI * 3.0) * 1.8);
        double humidity = round1(61.0 + (temperature - safeMid) * 2.2 + Math.cos(progress * Math.PI * 4.0) * 1.6);

        return new TelemetryRecord(
                vehicle.vehicleCode(),
                startTime.plusSeconds(elapsedSeconds),
                round1(temperature),
                humidity,
                doorOpen,
                round1(speed),
                outsideTemp,
                round3(interpolate(startLng, endLng, progress)),
                round3(interpolate(startLat, endLat, progress)),
                remainingKm,
                buildTrendLabel(temperature, vehicle.safeTempMax(), completed));
    }

    public void resetSession() {
        sessionStartTimes.clear();
    }

    private TravelState simulateTravel(double totalKm, long elapsedSeconds, long expectedDurationSeconds, boolean risky) {
        double travelledKm = 0.0;
        long simulatedSeconds = 0L;

        while (simulatedSeconds < elapsedSeconds && travelledKm < totalKm) {
            long stepSeconds = Math.min(SIMULATION_STEP_SECONDS, elapsedSeconds - simulatedSeconds);
            double phase = Math.min(simulatedSeconds / (double) expectedDurationSeconds, 1.0);
            double progress = Math.min(travelledKm / totalKm, 1.0);
            boolean doorOpen = isDoorOpen(phase, risky);
            double speed = doorOpen ? 0.0 : computeSpeed(phase, risky);
            travelledKm += speed * (stepSeconds / 3600.0);
            simulatedSeconds += stepSeconds;
        }

        double remainingKm = Math.max(0.0, totalKm - travelledKm);
        boolean completed = remainingKm <= 0.0;
        double progress = Math.min((totalKm - remainingKm) / totalKm, 1.0);
        double phase = Math.min(elapsedSeconds / (double) expectedDurationSeconds, 1.0);
        boolean doorOpen = !completed && isDoorOpen(phase, risky);
        double speed = completed ? 0.0 : (doorOpen ? 0.0 : computeSpeed(phase, risky));

        return new TravelState(progress, round1(remainingKm), round1(speed), doorOpen, completed);
    }

    private double riskyTemperature(double safeMid, double safeMax, double progress) {
        double value;
        if (progress < 0.24) {
            value = safeMid + progress * 1.6;
        } else if (progress < 0.58) {
            value = safeMid + 0.5 + (progress - 0.24) * 3.2;
        } else if (progress < 0.82) {
            value = safeMax - 0.8 + (progress - 0.58) * 3.8;
        } else {
            value = safeMax - 0.2 - (progress - 0.82) * 2.0;
        }
        return value + Math.sin(progress * Math.PI * 7.0) * 0.16;
    }

    private double stableTemperature(double safeMid, double progress) {
        return safeMid + Math.sin(progress * Math.PI * 4.0) * 0.28 + Math.cos(progress * Math.PI * 1.4) * 0.12;
    }

    private boolean isDoorOpen(double phase, boolean risky) {
        if (risky) {
            return inRange(phase, 0.16, 0.18) || inRange(phase, 0.43, 0.46) || inRange(phase, 0.67, 0.70);
        }
        return inRange(phase, 0.52, 0.54);
    }

    private double computeSpeed(double progress, boolean risky) {
        double base = risky ? 39.0 : 44.0;
        return base + Math.sin(progress * Math.PI * 5.0) * 7.5 + Math.cos(progress * Math.PI * 2.4) * 3.5;
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
        if (temperature >= safeTempMax - 1.2) {
            return "趋势预警";
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

    private record TravelState(
            double progress,
            double remainingKm,
            double speedKmh,
            boolean doorOpen,
            boolean completed) {
    }
}
