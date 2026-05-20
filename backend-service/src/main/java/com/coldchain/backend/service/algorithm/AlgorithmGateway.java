package com.coldchain.backend.service.algorithm;

import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import java.util.List;

public interface AlgorithmGateway {
    AlgorithmEvaluation evaluate(
            String vehicleCode,
            Vehicle vehicle,
            TelemetryRecord latestTelemetry,
            List<TelemetryRecord> telemetryHistory,
            List<AlertRecord> alerts);

    AlgorithmGatewayStatus status();
}
