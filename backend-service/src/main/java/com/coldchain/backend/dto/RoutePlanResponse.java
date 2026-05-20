package com.coldchain.backend.dto;

public record RoutePlanResponse(
        String vehicleCode,
        String planType,
        String planTitle,
        String planDetail,
        String estimatedCost,
        String estimatedBenefit,
        boolean recommended,
        String createdTime) {
}
