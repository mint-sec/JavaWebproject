package com.coldchain.backend.dto;

public record WorkspaceAlertResponse(
        String id,
        String title,
        String level,
        String levelLabel,
        String detail,
        String vehicleId,
        String owner,
        String ownerUserId,
        String status,
        String note,
        String domain,
        String handledAt) {
}
