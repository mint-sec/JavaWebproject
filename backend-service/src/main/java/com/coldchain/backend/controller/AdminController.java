package com.coldchain.backend.controller;

import com.coldchain.backend.dto.AdminAlertResponse;
import com.coldchain.backend.dto.AdminAlertUpdateRequest;
import com.coldchain.backend.dto.AdminConsoleResponse;
import com.coldchain.backend.dto.AdminVehicleRequest;
import com.coldchain.backend.dto.AdminVehicleResponse;
import com.coldchain.backend.dto.ApiResponse;
import com.coldchain.backend.dto.UpdateUserRequest;
import com.coldchain.backend.dto.UserResponse;
import com.coldchain.backend.service.AdminService;
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
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/console")
    public ApiResponse<AdminConsoleResponse> console() {
        return ApiResponse.ok(adminService.getConsole());
    }

    @PatchMapping("/users/{userId}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(adminService.updateUser(userId, request));
    }

    @PostMapping("/vehicles")
    public ApiResponse<AdminVehicleResponse> createVehicle(@Valid @RequestBody AdminVehicleRequest request) {
        return ApiResponse.ok(adminService.createVehicle(request));
    }

    @PutMapping("/vehicles/{vehicleId}")
    public ApiResponse<AdminVehicleResponse> updateVehicle(
            @PathVariable String vehicleId,
            @Valid @RequestBody AdminVehicleRequest request) {
        return ApiResponse.ok(adminService.updateVehicle(vehicleId, request));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    public ApiResponse<Void> deleteVehicle(@PathVariable String vehicleId) {
        adminService.deleteVehicle(vehicleId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/alerts/{alertId}")
    public ApiResponse<AdminAlertResponse> updateAlert(
            @PathVariable String alertId,
            @Valid @RequestBody AdminAlertUpdateRequest request) {
        return ApiResponse.ok(adminService.updateAlert(alertId, request));
    }
}
