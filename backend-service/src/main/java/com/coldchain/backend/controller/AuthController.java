package com.coldchain.backend.controller;

import com.coldchain.backend.dto.ApiResponse;
import com.coldchain.backend.dto.LoginRequest;
import com.coldchain.backend.dto.RegisterRequest;
import com.coldchain.backend.dto.SessionResponse;
import com.coldchain.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<SessionResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<SessionResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<SessionResponse> me(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        return ApiResponse.ok(authService.me(userId));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok(null);
    }
}
