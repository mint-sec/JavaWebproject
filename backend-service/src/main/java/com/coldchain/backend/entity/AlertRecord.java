package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record AlertRecord(
        String alertId,
        String vehicleCode,
        String level,
        String alertType,
        String title,
        String detail,
        String suggestion,
        LocalDateTime triggerTime,
        String status,
        String owner,
        String processStatus,
        String note,
        LocalDateTime handledAt) {
}
