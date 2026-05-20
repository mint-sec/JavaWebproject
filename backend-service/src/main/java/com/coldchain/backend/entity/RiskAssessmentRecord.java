package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record RiskAssessmentRecord(
        String vehicleCode,
        double riskScore,
        String riskLevel,
        String riskLabel,
        String riskReason,
        Integer predictedMinutesToLimit,
        LocalDateTime assessmentTime,
        String algorithmVersion,
        String algorithmSource) {
}
