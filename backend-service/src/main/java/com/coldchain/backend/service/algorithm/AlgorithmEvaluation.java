package com.coldchain.backend.service.algorithm;

import java.util.List;

public record AlgorithmEvaluation(
        boolean anomalyDetected,
        String anomalyType,
        String anomalyReason,
        Integer predictedMinutesToLimit,
        double riskScore,
        String riskLevel,
        String riskLabel,
        String algorithmVersion,
        String algorithmSource,
        List<AlgorithmRecommendation> recommendations) {
}
