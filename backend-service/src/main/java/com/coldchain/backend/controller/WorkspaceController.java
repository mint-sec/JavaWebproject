package com.coldchain.backend.controller;

import com.coldchain.backend.dto.AdminAlertUpdateRequest;
import com.coldchain.backend.dto.AdminVehicleRequest;
import com.coldchain.backend.dto.ApiResponse;
import com.coldchain.backend.dto.WorkspaceAlertResponse;
import com.coldchain.backend.dto.WorkspaceConsoleResponse;
import com.coldchain.backend.dto.WorkspaceVehicleResponse;
import com.coldchain.backend.service.WorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspace")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/console")
    public ApiResponse<WorkspaceConsoleResponse> getConsole(HttpServletRequest request) {
        return ApiResponse.ok(workspaceService.getConsole((String) request.getAttribute("userId")));
    }

    @PostMapping("/vehicles")
    public ApiResponse<WorkspaceVehicleResponse> createVehicle(
            @Valid @RequestBody AdminVehicleRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(workspaceService.createVehicle((String) httpServletRequest.getAttribute("userId"), request));
    }

    @PutMapping("/vehicles/{vehicleId}")
    public ApiResponse<WorkspaceVehicleResponse> updateVehicle(
            @PathVariable String vehicleId,
            @Valid @RequestBody AdminVehicleRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(workspaceService.updateVehicle((String) httpServletRequest.getAttribute("userId"), vehicleId, request));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    public ApiResponse<Void> deleteVehicle(@PathVariable String vehicleId, HttpServletRequest request) {
        workspaceService.deleteVehicle((String) request.getAttribute("userId"), vehicleId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/alerts/{alertId}")
    public ApiResponse<WorkspaceAlertResponse> updateAlert(
            @PathVariable String alertId,
            @Valid @RequestBody AdminAlertUpdateRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(workspaceService.updateAlert((String) httpServletRequest.getAttribute("userId"), alertId, request));
    }
}
