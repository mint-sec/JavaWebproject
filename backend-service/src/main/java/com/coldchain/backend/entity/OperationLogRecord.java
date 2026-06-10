package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record OperationLogRecord(
        String logId,
        String moduleName,
        String actionName,
        String operatorName,
        String targetName,
        String result,
        String detail,
        LocalDateTime createdAt) {
}
