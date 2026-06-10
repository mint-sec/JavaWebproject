package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record LoginLogRecord(
        String logId,
        String account,
        String roleLabel,
        String result,
        String ip,
        String detail,
        LocalDateTime createdAt) {
}
