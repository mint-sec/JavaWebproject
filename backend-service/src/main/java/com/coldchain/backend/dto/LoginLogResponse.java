package com.coldchain.backend.dto;

public record LoginLogResponse(
        String id,
        String time,
        String account,
        String roleLabel,
        String result,
        String ip,
        String detail) {
}
