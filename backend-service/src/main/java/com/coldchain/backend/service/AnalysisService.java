package com.coldchain.backend.service;

import com.coldchain.backend.dto.AlertSummaryResponse;
import com.coldchain.backend.dto.AlgorithmStatusResponse;
import com.coldchain.backend.dto.RiskAssessmentResponse;
import com.coldchain.backend.dto.RoutePlanResponse;
import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.RiskAssessmentRecord;
import com.coldchain.backend.entity.RoutePlanRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import com.coldchain.backend.exception.NotFoundException;
import com.coldchain.backend.repository.MockDataRepository;
import com.coldchain.backend.service.algorithm.AlgorithmEvaluation;
import com.coldchain.backend.service.algorithm.AlgorithmGateway;
import com.coldchain.backend.service.algorithm.AlgorithmGatewayStatus;
import com.coldchain.backend.service.algorithm.AlgorithmRecommendation;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MockDataRepository repository;
    private final VehicleService vehicleService;
    private final SimulationTimelineService simulationTimelineService;
    private final AlgorithmGateway algorithmGateway;

    public AnalysisService(
            MockDataRepository repository,
            VehicleService vehicleService,
            SimulationTimelineService simulationTimelineService,
            AlgorithmGateway algorithmGateway) {
        this.repository = repository;
        this.vehicleService = vehicleService;
        this.simulationTimelineService = simulationTimelineService;
        this.algorithmGateway = algorithmGateway;
    }

    public AlertSummaryResponse getAlertSummary(String vehicleCode) {
        vehicleService.getVehicleEntity(vehicleCode);
        List<AlertRecord> alerts = simulationTimelineService.getCurrentAlerts(vehicleCode);
        if (alerts.isEmpty()) {
            return new AlertSummaryResponse(vehicleCode, 0, "NONE", false, null, null);
        }

        AlertRecord latest = alerts.get(0);
        String highestLevel = alerts.stream()
                .map(AlertRecord::level)
                .max(Comparator.comparingInt(this::alertPriority))
                .orElse("LOW");

        return new AlertSummaryResponse(
                vehicleCode,
                (int) alerts.stream().filter(alert -> "OPEN".equals(alert.status())).count(),
                highestLevel,
                alerts.stream().anyMatch(alert -> "HIGH".equals(alert.level())),
                latest.title(),
                latest.triggerTime().format(FULL_TIME));
    }

    public RiskAssessmentResponse getLatestRiskAssessment(String vehicleCode) {
        Vehicle vehicle = vehicleService.getVehicleEntity(vehicleCode);
        List<RiskAssessmentRecord> history = simulationTimelineService.getCurrentRiskAssessments(vehicleCode);
        if (!history.isEmpty()) {
            return toRiskResponse(history.get(0));
        }
        return evaluateRisk(vehicleCode, vehicle);
    }

    public List<RiskAssessmentResponse> getRiskAssessmentHistory(String vehicleCode, Integer limit) {
        vehicleService.getVehicleEntity(vehicleCode);
        List<RiskAssessmentResponse> results = simulationTimelineService.getCurrentRiskAssessments(vehicleCode).stream()
                .map(this::toRiskResponse)
                .toList();
        if (limit == null || limit <= 0 || limit >= results.size()) {
            return results;
        }
        return results.subList(0, limit);
    }

    public RoutePlanResponse getLatestRoutePlan(String vehicleCode) {
        vehicleService.getVehicleEntity(vehicleCode);
        List<RoutePlanRecord> plans = simulationTimelineService.getCurrentRoutePlans(vehicleCode);
        if (!plans.isEmpty()) {
            return toRoutePlanResponse(plans.get(0));
        }
        return buildFallbackRoutePlan(vehicleCode);
    }

    public List<RoutePlanResponse> getRoutePlans(String vehicleCode, Integer limit) {
        vehicleService.getVehicleEntity(vehicleCode);
        List<RoutePlanResponse> results = simulationTimelineService.getCurrentRoutePlans(vehicleCode).stream()
                .map(this::toRoutePlanResponse)
                .toList();
        if (results.isEmpty()) {
            results = List.of(buildFallbackRoutePlan(vehicleCode));
        }
        if (limit == null || limit <= 0 || limit >= results.size()) {
            return results;
        }
        return results.subList(0, limit);
    }

    public AlgorithmStatusResponse getAlgorithmStatus() {
        AlgorithmGatewayStatus status = algorithmGateway.status();
        return new AlgorithmStatusResponse(
                status.serviceName(),
                status.mode(),
                status.available(),
                status.fallbackEnabled(),
                status.algorithmVersion(),
                status.message());
    }

    private RiskAssessmentResponse evaluateRisk(String vehicleCode, Vehicle vehicle) {
        TelemetryRecord latest = simulationTimelineService.getCurrentTelemetry(vehicleCode);
        List<TelemetryRecord> telemetryHistory = simulationTimelineService.getCurrentTelemetryHistory(vehicleCode, Integer.MAX_VALUE);
        List<AlertRecord> alerts = simulationTimelineService.getCurrentAlerts(vehicleCode);
        AlgorithmEvaluation evaluation = algorithmGateway.evaluate(vehicleCode, vehicle, latest, telemetryHistory, alerts);

        return new RiskAssessmentResponse(
                vehicleCode,
                evaluation.riskScore(),
                evaluation.riskLevel(),
                evaluation.riskLabel(),
                evaluation.anomalyReason(),
                evaluation.predictedMinutesToLimit(),
                latest.recordTime().format(FULL_TIME),
                evaluation.algorithmVersion(),
                evaluation.algorithmSource());
    }

    private RoutePlanResponse buildFallbackRoutePlan(String vehicleCode) {
        RiskAssessmentResponse risk = getLatestRiskAssessment(vehicleCode);
        List<AlgorithmRecommendation> recommendations = buildDynamicRecommendations(vehicleCode);
        AlgorithmRecommendation recommendation = recommendations.stream()
                .filter(AlgorithmRecommendation::recommended)
                .findFirst()
                .orElse(recommendations.get(0));
        return new RoutePlanResponse(
                vehicleCode,
                recommendation.planType(),
                recommendation.title(),
                recommendation.detail() + " 当前风险等级：" + risk.riskLabel(),
                recommendation.estimatedCost(),
                recommendation.estimatedBenefit(),
                recommendation.recommended(),
                risk.assessmentTime());
    }

    public List<AlgorithmRecommendation> buildDynamicRecommendations(String vehicleCode) {
        Vehicle vehicle = vehicleService.getVehicleEntity(vehicleCode);
        TelemetryRecord latest = simulationTimelineService.getCurrentTelemetry(vehicleCode);
        List<TelemetryRecord> history = simulationTimelineService.getCurrentTelemetryHistory(vehicleCode, Integer.MAX_VALUE);
        List<AlertRecord> alerts = simulationTimelineService.getCurrentAlerts(vehicleCode);
        AlgorithmEvaluation evaluation = algorithmGateway.evaluate(vehicleCode, vehicle, latest, history, alerts);
        return new ArrayList<>(evaluation.recommendations());
    }

    private RiskAssessmentResponse toRiskResponse(RiskAssessmentRecord record) {
        return new RiskAssessmentResponse(
                record.vehicleCode(),
                record.riskScore(),
                record.riskLevel(),
                record.riskLabel(),
                record.riskReason(),
                record.predictedMinutesToLimit(),
                record.assessmentTime().format(FULL_TIME),
                record.algorithmVersion(),
                record.algorithmSource());
    }

    private RoutePlanResponse toRoutePlanResponse(RoutePlanRecord record) {
        return new RoutePlanResponse(
                record.vehicleCode(),
                record.planType(),
                record.planTitle(),
                record.planDetail(),
                record.estimatedCost(),
                record.estimatedBenefit(),
                record.recommended(),
                record.createdTime().format(FULL_TIME));
    }

    private int alertPriority(String level) {
        return switch (level) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}
