package com.coldchain.backend.dto;

import java.util.List;

public record WorkspaceConsoleResponse(
        List<OverviewCard> overviewCards,
        List<WorkspaceVehicleResponse> vehicles,
        List<WorkspaceAlertResponse> alerts) {

    public record OverviewCard(String label, String value, String detail) {
    }
}
