package com.coldchain.backend.dto;

public record AlertResponse(
        String alertId,
        String vehicleCode,
        String level,
        String title,
        String detail,
        String suggestion,
        String triggerTime) {
}
