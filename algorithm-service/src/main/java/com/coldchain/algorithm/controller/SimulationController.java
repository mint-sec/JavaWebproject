package com.coldchain.algorithm.controller;

import com.coldchain.algorithm.data.DataGenerator;
import com.coldchain.algorithm.model.EvaluateRequest;
import com.coldchain.algorithm.service.RealtimeSimulationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulation")
public class SimulationController {
    private final RealtimeSimulationService realtimeSimulationService;

    public SimulationController(RealtimeSimulationService realtimeSimulationService) {
        this.realtimeSimulationService = realtimeSimulationService;
    }

    @GetMapping("/telemetry")
    public Map<String, List<EvaluateRequest.TelemetryPayload>> generateTelemetry() {
        Map<String, List<EvaluateRequest.TelemetryPayload>> result = new LinkedHashMap<>();
        result.put("CC-VA-01", DataGenerator.generateCcVa01());
        result.put("CC-VA-02", DataGenerator.generateNormalVehicle(5.0));
        result.put("CC-VA-03", DataGenerator.generateNormalVehicle(5.8));
        result.put("CC-VA-04", DataGenerator.generateNormalVehicle(4.7));
        result.put("CC-VA-05", DataGenerator.generateNormalVehicle(6.6));
        return result;
    }

    @GetMapping("/realtime")
    public Map<String, EvaluateRequest.TelemetryPayload> currentTelemetry() {
        return realtimeSimulationService.getAllCurrentTelemetry();
    }

    @GetMapping("/realtime/{vehicleCode}")
    public EvaluateRequest.TelemetryPayload currentTelemetryByVehicle(@PathVariable String vehicleCode) {
        return realtimeSimulationService.getCurrentTelemetry(vehicleCode);
    }
}
