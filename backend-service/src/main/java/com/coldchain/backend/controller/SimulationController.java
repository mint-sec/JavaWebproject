package com.coldchain.backend.controller;

import com.coldchain.backend.dto.ApiResponse;
import com.coldchain.backend.service.AlgorithmAnalysisSyncService;
import com.coldchain.backend.service.AlgorithmSimulationSyncService;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("mysql")
@RequestMapping("/api/v1/simulation")
public class SimulationController {
    private final AlgorithmAnalysisSyncService algorithmAnalysisSyncService;
    private final AlgorithmSimulationSyncService algorithmSimulationSyncService;

    public SimulationController(
            AlgorithmSimulationSyncService algorithmSimulationSyncService,
            AlgorithmAnalysisSyncService algorithmAnalysisSyncService) {
        this.algorithmSimulationSyncService = algorithmSimulationSyncService;
        this.algorithmAnalysisSyncService = algorithmAnalysisSyncService;
    }

    @PostMapping("/import-telemetry")
    public ApiResponse<Map<String, Object>> importTelemetry() {
        int count = algorithmSimulationSyncService.syncTelemetryFromAlgorithm();
        return ApiResponse.ok(Map.of(
                "importedCount", count,
                "source", "algorithm-service/DataGenerator"));
    }

    @PostMapping("/import-analysis")
    public ApiResponse<Map<String, Object>> importAnalysis() {
        AlgorithmAnalysisSyncService.SyncResult result = algorithmAnalysisSyncService.syncAnalysisToDatabase();
        return ApiResponse.ok(Map.of(
                "riskCount", result.riskCount(),
                "routeCount", result.routeCount(),
                "source", "algorithm-service/evaluate"));
    }
}
