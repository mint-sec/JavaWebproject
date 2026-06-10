package com.coldchain.backend.dto;

public record ServiceMonitorResponse(
        String id,
        String name,
        String status,
        String tone,
        String latency,
        String source,
        String checkedAt,
        String detail) {
}
