package com.coldchain.backend.dto;

public record RiskAssessmentResponse(
        String vehicleCode,
        double riskScore,
        String riskLevel,
        String riskLabel,
        String riskReason,
        Integer predictedMinutesToLimit,
        String assessmentTime,
        String algorithmVersion,
        String algorithmSource) {
}
