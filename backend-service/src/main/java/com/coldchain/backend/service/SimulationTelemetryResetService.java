package com.coldchain.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("mysql")
public class SimulationTelemetryResetService {
    private final JdbcTemplate jdbcTemplate;
    private final VehicleSimulationService vehicleSimulationService;
    private final boolean resetTelemetryOnStartup;

    public SimulationTelemetryResetService(
            JdbcTemplate jdbcTemplate,
            VehicleSimulationService vehicleSimulationService,
            @Value("${app.simulation.reset-telemetry-on-startup:true}") boolean resetTelemetryOnStartup) {
        this.jdbcTemplate = jdbcTemplate;
        this.vehicleSimulationService = vehicleSimulationService;
        this.resetTelemetryOnStartup = resetTelemetryOnStartup;
    }

    @PostConstruct
    public void resetTelemetryData() {
        vehicleSimulationService.resetSession();
        if (!resetTelemetryOnStartup) {
            return;
        }

        jdbcTemplate.execute("DELETE FROM telemetry_records");
        jdbcTemplate.execute("DELETE FROM risk_assessments");
        jdbcTemplate.execute("DELETE FROM route_plans");
    }
}
