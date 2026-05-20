package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record RoutePlanRecord(
        String vehicleCode,
        String planType,
        String planTitle,
        String planDetail,
        String estimatedCost,
        String estimatedBenefit,
        boolean recommended,
        LocalDateTime createdTime) {
}
