package com.coldchain.backend.service;

import com.coldchain.backend.entity.mysql.TelemetryRecordEntity;
import com.coldchain.backend.repository.mysql.TelemetryRecordJpaRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Profile("mysql")
public class AlgorithmSimulationSyncService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient restClient;
    private final TelemetryRecordJpaRepository telemetryRecordJpaRepository;
    private final String simulationTelemetryPath;

    public AlgorithmSimulationSyncService(
            RestClient.Builder restClientBuilder,
            TelemetryRecordJpaRepository telemetryRecordJpaRepository,
            @Value("${app.algorithm.base-url:http://localhost:5001}") String baseUrl,
            @Value("${app.algorithm.simulation-telemetry-path:/simulation/telemetry}") String simulationTelemetryPath) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.telemetryRecordJpaRepository = telemetryRecordJpaRepository;
        this.simulationTelemetryPath = simulationTelemetryPath;
    }

    public int syncTelemetryFromAlgorithm() {
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> payload = restClient.get()
                .uri(simulationTelemetryPath)
                .retrieve()
                .body(Map.class);

        if (payload == null || payload.isEmpty()) {
            return 0;
        }

        List<TelemetryRecordEntity> entities = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : payload.entrySet()) {
            String vehicleCode = entry.getKey();
            for (Map<String, Object> item : entry.getValue()) {
                TelemetryRecordEntity entity = new TelemetryRecordEntity();
                entity.setVehicleCode(vehicleCode);
                entity.setRecordTime(LocalDateTime.parse(String.valueOf(item.get("recordTime")), FULL_TIME));
                entity.setTemperature(((Number) item.get("temperature")).doubleValue());
                entity.setHumidity(((Number) item.get("humidity")).doubleValue());
                entity.setDoorOpen(Boolean.TRUE.equals(item.get("doorOpen")));
                entity.setSpeed(((Number) item.get("speed")).doubleValue());
                entity.setOutsideTemp(((Number) item.get("outsideTemp")).doubleValue());
                entity.setLng(((Number) item.get("lng")).doubleValue());
                entity.setLat(((Number) item.get("lat")).doubleValue());
                entity.setRemainingKm(((Number) item.get("remainingKm")).doubleValue());
                entity.setTrend(String.valueOf(item.get("trend")));
                entities.add(entity);
            }
        }

        telemetryRecordJpaRepository.deleteAll();
        telemetryRecordJpaRepository.saveAll(entities);
        return entities.size();
    }
}
