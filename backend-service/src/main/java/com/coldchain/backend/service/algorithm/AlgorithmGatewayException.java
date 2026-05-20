package com.coldchain.backend.service.algorithm;

public class AlgorithmGatewayException extends RuntimeException {
    public AlgorithmGatewayException(String message) {
        super(message);
    }

    public AlgorithmGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
