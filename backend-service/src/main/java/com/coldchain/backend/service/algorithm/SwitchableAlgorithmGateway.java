package com.coldchain.backend.service.algorithm;

import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SwitchableAlgorithmGateway implements AlgorithmGateway {
    private final MockAlgorithmEngine mockAlgorithmEngine;
    private final HttpAlgorithmGateway httpAlgorithmGateway;
    private final String algorithmMode;
    private final boolean fallbackEnabled;

    public SwitchableAlgorithmGateway(
            MockAlgorithmEngine mockAlgorithmEngine,
            HttpAlgorithmGateway httpAlgorithmGateway,
            @Value("${app.algorithm.mode:mock-http-gateway}") String algorithmMode,
            @Value("${app.algorithm.fallback-enabled:true}") boolean fallbackEnabled) {
        this.mockAlgorithmEngine = mockAlgorithmEngine;
        this.httpAlgorithmGateway = httpAlgorithmGateway;
        this.algorithmMode = algorithmMode;
        this.fallbackEnabled = fallbackEnabled;
    }

    @Override
    public AlgorithmEvaluation evaluate(
            String vehicleCode,
            Vehicle vehicle,
            TelemetryRecord latestTelemetry,
            List<TelemetryRecord> telemetryHistory,
            List<AlertRecord> alerts) {
        if (!"http".equalsIgnoreCase(algorithmMode)) {
            return mockAlgorithmEngine.evaluate(vehicleCode, vehicle, latestTelemetry, telemetryHistory, alerts);
        }

        try {
            return httpAlgorithmGateway.evaluate(vehicleCode, vehicle, latestTelemetry, telemetryHistory, alerts);
        } catch (AlgorithmGatewayException exception) {
            if (!fallbackEnabled) {
                throw exception;
            }
            return mockAlgorithmEngine.evaluate(vehicleCode, vehicle, latestTelemetry, telemetryHistory, alerts);
        }
    }

    @Override
    public AlgorithmGatewayStatus status() {
        if (!"http".equalsIgnoreCase(algorithmMode)) {
            return mockAlgorithmEngine.status(fallbackEnabled, algorithmMode);
        }

        try {
            boolean available = httpAlgorithmGateway.ping();
            return httpAlgorithmGateway.status(fallbackEnabled, available, algorithmMode);
        } catch (Exception exception) {
            return fallbackEnabled
                    ? mockAlgorithmEngine.status(true, algorithmMode + "-fallback")
                    : httpAlgorithmGateway.status(false, false, algorithmMode);
        }
    }
}
