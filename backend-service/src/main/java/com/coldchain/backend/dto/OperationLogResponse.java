package com.coldchain.backend.dto;

public record OperationLogResponse(
        String id,
        String time,
        String module,
        String action,
        String operator,
        String target,
        String result,
        String detail) {
}
