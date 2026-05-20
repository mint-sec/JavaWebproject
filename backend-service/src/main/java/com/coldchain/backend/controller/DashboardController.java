package com.coldchain.backend.controller;

import com.coldchain.backend.dto.ApiResponse;
import com.coldchain.backend.dto.DashboardResponse;
import com.coldchain.backend.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/vehicles/{vehicleCode}")
    public ApiResponse<DashboardResponse> getVehicleDashboard(@PathVariable String vehicleCode) {
        return ApiResponse.ok(dashboardService.getVehicleDashboard(vehicleCode));
    }
}
