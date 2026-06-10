package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskScorerTest {

    private final RiskScorer scorer = new RiskScorer();

    @Test
    void shouldReturnLowRiskForNormalTemp() {
        EvaluateRequest req = buildRequest(4.6, false, 20.0, 0, 20.0);
        RiskResult result = scorer.calculate(req, AnomalyResult.normal());

        assertTrue(result.getScore() < 45.0);
        assertEquals("LOW", result.getLevel());
    }

    @Test
    void shouldReturnHighRiskForHighTemp() {
        EvaluateRequest req = buildRequest(7.5, true, 13.4, 2, 35.0);
        AnomalyResult anomaly = new AnomalyResult(true, "TREND_RISE", "test", 12, 0.1);
        RiskResult result = scorer.calculate(req, anomaly);

        assertTrue(result.getScore() >= 80.0);
        assertEquals("HIGH", result.getLevel());
    }

    @Test
    void shouldIncreaseScoreWhenDoorOpen() {
        EvaluateRequest reqClosed = buildRequest(5.0, false, 20.0, 0, 20.0);
        EvaluateRequest reqOpen = buildRequest(5.0, true, 20.0, 0, 20.0);

        double scoreClosed = scorer.calculate(reqClosed, AnomalyResult.normal()).getScore();
        double scoreOpen = scorer.calculate(reqOpen, AnomalyResult.normal()).getScore();

        assertEquals(12.0, scoreOpen - scoreClosed, 0.01);
    }

    @Test
    void shouldNotExceedMaxScore() {
        EvaluateRequest req = buildRequest(15.0, true, 40.0, 10, 40.0);
        AnomalyResult anomaly = new AnomalyResult(true, "THRESHOLD_BREACH", "test", 5, 0.2);
        RiskResult result = scorer.calculate(req, anomaly);

        assertTrue(result.getScore() <= 98.0);
    }

    private EvaluateRequest buildRequest(double temp, boolean doorOpen, double remainingKm, int alertCount, double outsideTemp) {
        EvaluateRequest req = new EvaluateRequest();

        EvaluateRequest.VehiclePayload vehicle = new EvaluateRequest.VehiclePayload();
        vehicle.setSafeTempMin(2.0);
        vehicle.setSafeTempMax(8.0);
        req.setVehicle(vehicle);

        EvaluateRequest.TelemetryPayload telemetry = new EvaluateRequest.TelemetryPayload();
        telemetry.setTemperature(temp);
        telemetry.setDoorOpen(doorOpen);
        telemetry.setRemainingKm(remainingKm);
        telemetry.setOutsideTemp(outsideTemp);
        req.setLatestTelemetry(telemetry);

        List<EvaluateRequest.AlertPayload> alerts = new ArrayList<>();
        for (int i = 0; i < alertCount; i++) {
            EvaluateRequest.AlertPayload alert = new EvaluateRequest.AlertPayload();
            alert.setAlertId("ALT-" + i);
            alert.setLevel("HIGH");
            alert.setAlertType("TREND_WARNING");
            alerts.add(alert);
        }
        req.setAlerts(alerts);

        return req;
    }
}
