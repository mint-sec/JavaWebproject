package com.coldchain.backend.controller;

import com.coldchain.backend.dto.AlertSummaryResponse;
import com.coldchain.backend.dto.AlgorithmStatusResponse;
import com.coldchain.backend.dto.ApiResponse;
import com.coldchain.backend.dto.RiskAssessmentResponse;
import com.coldchain.backend.dto.RoutePlanResponse;
import com.coldchain.backend.service.AnalysisService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/vehicles/{vehicleCode}/alerts/summary")
    public ApiResponse<AlertSummaryResponse> getAlertSummary(@PathVariable String vehicleCode) {
        return ApiResponse.ok(analysisService.getAlertSummary(vehicleCode));
    }

    @GetMapping("/vehicles/{vehicleCode}/risk-assessments/latest")
    public ApiResponse<RiskAssessmentResponse> getLatestRiskAssessment(@PathVariable String vehicleCode) {
        return ApiResponse.ok(analysisService.getLatestRiskAssessment(vehicleCode));
    }

    @GetMapping("/vehicles/{vehicleCode}/risk-assessments/history")
    public ApiResponse<List<RiskAssessmentResponse>> getRiskAssessmentHistory(
            @PathVariable String vehicleCode,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(analysisService.getRiskAssessmentHistory(vehicleCode, limit));
    }

    @GetMapping("/vehicles/{vehicleCode}/route-plans/latest")
    public ApiResponse<RoutePlanResponse> getLatestRoutePlan(@PathVariable String vehicleCode) {
        return ApiResponse.ok(analysisService.getLatestRoutePlan(vehicleCode));
    }

    @GetMapping("/vehicles/{vehicleCode}/route-plans")
    public ApiResponse<List<RoutePlanResponse>> getRoutePlans(
            @PathVariable String vehicleCode,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(analysisService.getRoutePlans(vehicleCode, limit));
    }

    @GetMapping("/algorithm/status")
    public ApiResponse<AlgorithmStatusResponse> getAlgorithmStatus() {
        return ApiResponse.ok(analysisService.getAlgorithmStatus());
    }
}
