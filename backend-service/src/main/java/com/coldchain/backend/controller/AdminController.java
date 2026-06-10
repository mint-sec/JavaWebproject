package com.coldchain.backend.controller;

import com.coldchain.backend.dto.AdminConsoleResponse;
import com.coldchain.backend.dto.ApiResponse;
import com.coldchain.backend.dto.UpdateUserRequest;
import com.coldchain.backend.dto.UserResponse;
import com.coldchain.backend.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ApiResponse<AdminConsoleResponse> console(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return ApiResponse.ok(adminService.getConsole(username == null ? "admin" : username));
    }

    @PatchMapping("/users/{userId}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpServletRequest) {
        String operatorName = (String) httpServletRequest.getAttribute("username");
        return ApiResponse.ok(adminService.updateUser(userId, request, operatorName == null ? "admin" : operatorName));
    }
}
