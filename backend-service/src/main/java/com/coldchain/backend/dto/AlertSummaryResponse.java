package com.coldchain.backend.dto;

public record AlertSummaryResponse(
        String vehicleCode,
        int openAlertCount,
        String highestLevel,
        boolean hasHighRiskAlert,
        String latestTitle,
        String latestTriggerTime) {
}
