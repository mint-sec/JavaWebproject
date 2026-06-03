package com.coldchain.backend.entity;

import java.time.LocalDateTime;

public record UserRecord(
        String id,
        String username,
        String displayName,
        String phone,
        String email,
        String password,
        String role,
        String status,
        String origin,
        LocalDateTime createdAt) {
}
