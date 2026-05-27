package com.coldchain.backend.service;

import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import com.coldchain.backend.entity.mysql.RiskAssessmentEntity;
import com.coldchain.backend.entity.mysql.RoutePlanEntity;
import com.coldchain.backend.repository.mysql.RiskAssessmentJpaRepository;
import com.coldchain.backend.repository.mysql.RoutePlanJpaRepository;
import com.coldchain.backend.service.algorithm.AlgorithmEvaluation;
import com.coldchain.backend.service.algorithm.AlgorithmGateway;
import com.coldchain.backend.service.algorithm.AlgorithmRecommendation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mysql")
public class AlgorithmAnalysisSyncService {
    private final VehicleService vehicleService;
    private final SimulationTimelineService simulationTimelineService;
    private final AlgorithmGateway algorithmGateway;
    private final RiskAssessmentJpaRepository riskAssessmentJpaRepository;
    private final RoutePlanJpaRepository routePlanJpaRepository;

    public AlgorithmAnalysisSyncService(
            VehicleService vehicleService,
            SimulationTimelineService simulationTimelineService,
            AlgorithmGateway algorithmGateway,
            RiskAssessmentJpaRepository riskAssessmentJpaRepository,
            RoutePlanJpaRepository routePlanJpaRepository) {
        this.vehicleService = vehicleService;
        this.simulationTimelineService = simulationTimelineService;
        this.algorithmGateway = algorithmGateway;
        this.riskAssessmentJpaRepository = riskAssessmentJpaRepository;
        this.routePlanJpaRepository = routePlanJpaRepository;
    }

    public SyncResult syncAnalysisToDatabase() {
        List<Vehicle> vehicles = vehicleService.getVehicleEntities();
        List<RiskAssessmentEntity> riskEntities = new ArrayList<>();
        List<RoutePlanEntity> routeEntities = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            String vehicleCode = vehicle.vehicleCode();
            TelemetryRecord latest = simulationTimelineService.getCurrentTelemetry(vehicleCode);
            List<TelemetryRecord> history = simulationTimelineService.getCurrentTelemetryHistory(vehicleCode, Integer.MAX_VALUE);
            List<AlertRecord> alerts = simulationTimelineService.getCurrentAlerts(vehicleCode);
            AlgorithmEvaluation evaluation = algorithmGateway.evaluate(vehicleCode, vehicle, latest, history, alerts);

            RiskAssessmentEntity risk = new RiskAssessmentEntity();
            risk.setVehicleCode(vehicleCode);
            risk.setRiskScore(evaluation.riskScore());
            risk.setRiskLevel(evaluation.riskLevel());
            risk.setRiskLabel(evaluation.riskLabel());
            risk.setRiskReason(evaluation.anomalyReason());
            risk.setPredictedMinutesToLimit(evaluation.predictedMinutesToLimit());
            risk.setAlgorithmVersion(evaluation.algorithmVersion());
            risk.setAlgorithmSource(evaluation.algorithmSource());
            risk.setAssessmentTime(latest.recordTime());
            riskEntities.add(risk);

            for (AlgorithmRecommendation recommendation : evaluation.recommendations()) {
                RoutePlanEntity route = new RoutePlanEntity();
                route.setVehicleCode(vehicleCode);
                route.setPlanType(recommendation.planType());
                route.setPlanTitle(recommendation.title());
                route.setPlanDetail(recommendation.detail());
                route.setEstimatedCost(recommendation.estimatedCost());
                route.setEstimatedBenefit(recommendation.estimatedBenefit());
                route.setRecommended(recommendation.recommended());
                route.setCreatedTime(latest.recordTime());
                routeEntities.add(route);
            }
        }

        riskAssessmentJpaRepository.deleteAll();
        routePlanJpaRepository.deleteAll();
        riskAssessmentJpaRepository.saveAll(riskEntities);
        routePlanJpaRepository.saveAll(routeEntities);
        return new SyncResult(riskEntities.size(), routeEntities.size());
    }

    public record SyncResult(int riskCount, int routeCount) {
    }
}
