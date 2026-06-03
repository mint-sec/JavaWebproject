package com.coldchain.backend.dto;

public record UserResponse(
        String id,
        String username,
        String displayName,
        String phone,
        String email,
        String role,
        String roleLabel,
        String status,
        String origin) {
}
