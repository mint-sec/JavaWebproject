package com.coldchain.backend.web;

import com.coldchain.backend.model.AlertRecord;
import com.coldchain.backend.model.TelemetryRecord;
import com.coldchain.backend.model.Vehicle;
import com.coldchain.backend.service.ColdChainService;
import com.coldchain.backend.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ApiHandler implements HttpHandler {
    private final ColdChainService service;

    public ApiHandler(ColdChainService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, JsonUtil.error("Method not allowed"));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("/api/v1/vehicles".equals(path)) {
            handleVehicles(exchange);
            return;
        }

        String[] segments = path.split("/");
        if (segments.length >= 6
                && "api".equals(segments[1])
                && "v1".equals(segments[2])
                && "vehicles".equals(segments[3])) {
            String vehicleId = segments[4];
            String resource = segments[5];
            if ("telemetry".equals(resource) && segments.length >= 7 && "latest".equals(segments[6])) {
                handleLatestTelemetry(exchange, vehicleId);
                return;
            }
            if ("alerts".equals(resource)) {
                handleAlerts(exchange, vehicleId);
                return;
            }
        }

        writeResponse(exchange, 404, JsonUtil.error("Resource not found"));
    }

    private void handleVehicles(HttpExchange exchange) throws IOException {
        List<Vehicle> vehicles = service.getVehicles();
        writeResponse(exchange, 200, JsonUtil.ok(JsonUtil.toJsonArray(vehicles, Vehicle::toJson)));
    }

    private void handleLatestTelemetry(HttpExchange exchange, String vehicleId) throws IOException {
        TelemetryRecord record = service.getLatestTelemetry(vehicleId);
        if (record == null) {
            writeResponse(exchange, 404, JsonUtil.error("Vehicle telemetry not found"));
            return;
        }
        writeResponse(exchange, 200, JsonUtil.ok(record.toJson()));
    }

    private void handleAlerts(HttpExchange exchange, String vehicleId) throws IOException {
        Integer limit = extractLimit(exchange.getRequestURI().getQuery());
        List<AlertRecord> alerts = service.getAlerts(vehicleId, limit);
        writeResponse(exchange, 200, JsonUtil.ok(JsonUtil.toJsonArray(alerts, AlertRecord::toJson)));
    }

    private Integer extractLimit(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && "limit".equals(parts[0])) {
                try {
                    return Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private void writeResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
