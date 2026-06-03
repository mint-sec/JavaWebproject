package com.coldchain.backend.dto;

import java.util.List;

public record AdminConsoleResponse(
        List<OverviewCard> overviewCards,
        List<UserResponse> users,
        List<AdminVehicleResponse> vehicles,
        List<AdminAlertResponse> alerts) {

    public record OverviewCard(String label, String value, String detail) {
    }
}
