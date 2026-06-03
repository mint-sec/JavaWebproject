package com.coldchain.backend.dto;

public record AdminAlertResponse(
        String id,
        String title,
        String level,
        String levelLabel,
        String detail,
        String vehicleId,
        String owner,
        String status,
        String note,
        String handledAt) {
}
