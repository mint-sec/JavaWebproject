package com.coldchain.backend.dto;

public record AlgorithmStatusResponse(
        String serviceName,
        String mode,
        boolean available,
        boolean fallbackEnabled,
        String algorithmVersion,
        String message) {
}
