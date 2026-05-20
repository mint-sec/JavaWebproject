package com.coldchain.backend.service.algorithm;

public record AlgorithmGatewayStatus(
        String serviceName,
        String mode,
        boolean available,
        boolean fallbackEnabled,
        String algorithmVersion,
        String message) {
}
