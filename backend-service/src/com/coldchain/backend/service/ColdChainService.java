package com.coldchain.backend.service;

import com.coldchain.backend.model.AlertRecord;
import com.coldchain.backend.model.TelemetryRecord;
import com.coldchain.backend.model.Vehicle;
import com.coldchain.backend.repository.MockDataRepository;
import java.util.List;

public class ColdChainService {
    private final MockDataRepository repository;

    public ColdChainService(MockDataRepository repository) {
        this.repository = repository;
    }

    public List<Vehicle> getVehicles() {
        return repository.findAllVehicles();
    }

    public TelemetryRecord getLatestTelemetry(String vehicleId) {
        return repository.findLatestTelemetryByVehicleId(vehicleId);
    }

    public List<AlertRecord> getAlerts(String vehicleId, Integer limit) {
        List<AlertRecord> results = repository.findAlertsByVehicleId(vehicleId);
        if (limit == null || limit <= 0 || limit >= results.size()) {
            return results;
        }
        return results.subList(0, limit);
    }
}
