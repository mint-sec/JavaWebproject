package com.coldchain.backend.service.algorithm;

import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpAlgorithmGateway {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient restClient;
    private final String evaluatePath;
    private final String algorithmVersion;

    public HttpAlgorithmGateway(
            RestClient.Builder restClientBuilder,
            @Value("${app.algorithm.base-url:http://localhost:5001}") String baseUrl,
            @Value("${app.algorithm.evaluate-path:/evaluate}") String evaluatePath,
            @Value("${app.algorithm.version:http-risk-v1}") String algorithmVersion) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.evaluatePath = evaluatePath;
        this.algorithmVersion = algorithmVersion;
    }

    public AlgorithmEvaluation evaluate(
            String vehicleCode,
            Vehicle vehicle,
            TelemetryRecord latestTelemetry,
            List<TelemetryRecord> telemetryHistory,
            List<AlertRecord> alerts) {
        try {
            AlgorithmEvaluateRequest request = new AlgorithmEvaluateRequest(
                    vehicleCode,
                    new AlgorithmEvaluateRequest.VehiclePayload(
                            vehicle.vehicleCode(),
                            vehicle.cargoType(),
                            vehicle.cargoName(),
                            vehicle.safeTempMin(),
                            vehicle.safeTempMax(),
                            vehicle.status()),
                    new AlgorithmEvaluateRequest.TelemetryPayload(
                            latestTelemetry.recordTime().format(FULL_TIME),
                            latestTelemetry.temperature(),
                            latestTelemetry.humidity(),
                            latestTelemetry.doorOpen(),
                            latestTelemetry.speed(),
                            latestTelemetry.outsideTemp(),
                            latestTelemetry.lng(),
                            latestTelemetry.lat(),
                            latestTelemetry.remainingKm(),
                            latestTelemetry.trend()),
                    telemetryHistory.stream()
                            .map(item -> new AlgorithmEvaluateRequest.HistoryPayload(
                                    item.recordTime().format(FULL_TIME),
                                    item.temperature()))
                            .toList(),
                    alerts.stream()
                            .map(item -> new AlgorithmEvaluateRequest.AlertPayload(
                                    item.alertId(),
                                    item.level(),
                                    item.alertType(),
                                    item.triggerTime().format(FULL_TIME)))
                            .toList());

            AlgorithmHttpResponse response = restClient.post()
                    .uri(evaluatePath)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (clientRequest, clientResponse) -> {
                        throw new AlgorithmGatewayException("算法服务返回错误状态: " + clientResponse.getStatusCode());
                    })
                    .body(AlgorithmHttpResponse.class);

            if (response == null) {
                throw new AlgorithmGatewayException("算法服务返回空响应");
            }

            return new AlgorithmEvaluation(
                    response.anomalyDetected(),
                    response.anomalyType(),
                    response.anomalyReason(),
                    response.predictedMinutesToLimit(),
                    response.riskScore(),
                    response.riskLevel(),
                    response.riskLabel(),
                    response.algorithmVersion() == null ? algorithmVersion : response.algorithmVersion(),
                    response.algorithmSource() == null ? "PYTHON_HTTP" : response.algorithmSource(),
                    response.recommendations() == null
                            ? List.of()
                            : response.recommendations().stream()
                                    .map(item -> new AlgorithmRecommendation(
                                            item.planType(),
                                            item.title(),
                                            item.detail(),
                                            item.estimatedCost(),
                                            item.estimatedBenefit(),
                                            item.recommended()))
                                    .toList());
        } catch (Exception exception) {
            if (exception instanceof AlgorithmGatewayException gatewayException) {
                throw gatewayException;
            }
            throw new AlgorithmGatewayException("算法 HTTP 网关调用失败", exception);
        }
    }

    public AlgorithmGatewayStatus status(boolean fallbackEnabled, boolean available, String mode) {
        return new AlgorithmGatewayStatus(
                "coldchain-algorithm-gateway",
                mode,
                available,
                fallbackEnabled,
                algorithmVersion,
                available ? "已切换到 HTTP 算法网关" : "HTTP 算法网关未连通，当前将使用降级策略");
    }

    public record AlgorithmEvaluateRequest(
            String vehicleCode,
            VehiclePayload vehicle,
            TelemetryPayload latestTelemetry,
            List<HistoryPayload> telemetryHistory,
            List<AlertPayload> alerts) {
        public record VehiclePayload(
                String vehicleCode,
                String cargoType,
                String cargoName,
                double safeTempMin,
                double safeTempMax,
                String status) {
        }

        public record TelemetryPayload(
                String recordTime,
                double temperature,
                double humidity,
                boolean doorOpen,
                double speed,
                double outsideTemp,
                double lng,
                double lat,
                double remainingKm,
                String trend) {
        }

        public record HistoryPayload(String recordTime, double temperature) {
        }

        public record AlertPayload(
                String alertId,
                String level,
                String alertType,
                String triggerTime) {
        }
    }

    public record AlgorithmHttpResponse(
            boolean anomalyDetected,
            String anomalyType,
            String anomalyReason,
            Integer predictedMinutesToLimit,
            double riskScore,
            String riskLevel,
            String riskLabel,
            String algorithmVersion,
            String algorithmSource,
            List<RecommendationPayload> recommendations) {
        public record RecommendationPayload(
                String planType,
                String title,
                String detail,
                String estimatedCost,
                String estimatedBenefit,
                boolean recommended) {
        }
    }
}
