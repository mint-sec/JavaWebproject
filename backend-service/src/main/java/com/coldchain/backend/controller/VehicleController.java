package com.coldchain.backend.controller;

import com.coldchain.backend.dto.AlertResponse;
import com.coldchain.backend.dto.ApiResponse;
import com.coldchain.backend.dto.TelemetryLatestResponse;
import com.coldchain.backend.dto.TelemetryPointResponse;
import com.coldchain.backend.dto.VehicleResponse;
import com.coldchain.backend.service.VehicleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/vehicles")
    public ApiResponse<List<VehicleResponse>> getVehicles(HttpServletRequest request) {
        return ApiResponse.ok(vehicleService.getVehicles((String) request.getAttribute("userId")));
    }

    @GetMapping("/vehicles/{vehicleCode}/telemetry/latest")
    public ApiResponse<TelemetryLatestResponse> getLatestTelemetry(@PathVariable String vehicleCode, HttpServletRequest request) {
        return ApiResponse.ok(vehicleService.getLatestTelemetry((String) request.getAttribute("userId"), vehicleCode));
    }

    @GetMapping("/vehicles/{vehicleCode}/telemetry/history")
    public ApiResponse<List<TelemetryPointResponse>> getTelemetryHistory(
            @PathVariable String vehicleCode,
            @RequestParam(defaultValue = "30") int minutes,
            HttpServletRequest request) {
        return ApiResponse.ok(vehicleService.getTelemetryHistory((String) request.getAttribute("userId"), vehicleCode, minutes));
    }

    @GetMapping("/vehicles/{vehicleCode}/alerts")
    public ApiResponse<List<AlertResponse>> getVehicleAlerts(
            @PathVariable String vehicleCode,
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request) {
        return ApiResponse.ok(vehicleService.getVehicleAlerts((String) request.getAttribute("userId"), vehicleCode, limit));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<AlertResponse>> getAlerts(
            @RequestParam String vehicleCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        return ApiResponse.ok(vehicleService.getAlerts((String) request.getAttribute("userId"), vehicleCode, page, pageSize));
    }

    @GetMapping("/alerts/{alertId}")
    public ApiResponse<AlertResponse> getAlertDetail(@PathVariable String alertId, HttpServletRequest request) {
        return ApiResponse.ok(vehicleService.getAlertDetail((String) request.getAttribute("userId"), alertId));
    }
}
