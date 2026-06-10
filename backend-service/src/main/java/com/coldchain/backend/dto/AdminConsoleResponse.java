package com.coldchain.backend.dto;

import java.util.List;

public record AdminConsoleResponse(
        List<OverviewCard> overviewCards,
        List<OverviewCard> logCenterCards,
        List<UserResponse> users,
        List<LoginLogResponse> loginLogs,
        List<OperationLogResponse> operationLogs,
        List<ServiceMonitorResponse> serviceMonitors) {

    public record OverviewCard(String label, String value, String detail) {
    }
}
