package com.coldchain.backend.dto;

public record SessionResponse(
        String userId,
        String username,
        String displayName,
        String role,
        String roleLabel,
        String token,
        String loggedInAt) {
}
