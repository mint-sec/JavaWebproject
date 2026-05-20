package com.coldchain.backend.service.algorithm;

public record AlgorithmRecommendation(
        String planType,
        String title,
        String detail,
        String estimatedCost,
        String estimatedBenefit,
        boolean recommended) {
}
