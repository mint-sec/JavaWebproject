package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnomalyDetectorTest {

    private final AnomalyDetector detector = new AnomalyDetector();

    @Test
    void shouldReturnNormalWhenTempSafe() {
        EvaluateRequest req = buildRequest(4.6, 2.0, 8.0, buildHistory(4.4, 4.5, 4.3, 4.6, 4.4));
        AnomalyResult result = detector.detect(req);

        assertFalse(result.isDetected());
        assertEquals("NONE", result.getType());
    }

    @Test
    void shouldDetectThresholdBreach() {
        EvaluateRequest req = buildRequest(9.2, 2.0, 8.0, List.of());
        AnomalyResult result = detector.detect(req);

        assertTrue(result.isDetected());
        assertEquals("THRESHOLD_BREACH", result.getType());
    }

    @Test
    void shouldDetectTrendRise() {
        EvaluateRequest req = buildRequest(7.5, 2.0, 8.0, buildHistory(4.6, 4.9, 5.3, 6.1, 6.8));
        AnomalyResult result = detector.detect(req);

        assertTrue(result.isDetected());
        assertEquals("TREND_RISE", result.getType());
        assertTrue(result.getMinutesToLimit() > 0);
    }

    @Test
    void shouldDetectLowTemp() {
        EvaluateRequest req = buildRequest(1.5, 2.0, 8.0, List.of());
        AnomalyResult result = detector.detect(req);

        assertTrue(result.isDetected());
        assertEquals("THRESHOLD_BREACH", result.getType());
    }

    @Test
    void shouldCalcSlopeCorrectly() {
        double[] x = {0, 1, 2, 3, 4, 5};
        double[] y = {4.6, 4.9, 5.3, 6.1, 6.8, 7.5};
        double slope = AnomalyDetector.calcSlope(x, y);

        assertTrue(slope > 0.5);
    }

    private EvaluateRequest buildRequest(double temp, double safeMin, double safeMax, List<EvaluateRequest.HistoryPayload> history) {
        EvaluateRequest req = new EvaluateRequest();
        req.setVehicleCode("TEST-01");

        EvaluateRequest.VehiclePayload vehicle = new EvaluateRequest.VehiclePayload();
        vehicle.setSafeTempMin(safeMin);
        vehicle.setSafeTempMax(safeMax);
        vehicle.setCargoType("VACCINE");
        vehicle.setStatus("IN_TRANSIT");
        req.setVehicle(vehicle);

        EvaluateRequest.TelemetryPayload telemetry = new EvaluateRequest.TelemetryPayload();
        telemetry.setTemperature(temp);
        telemetry.setRecordTime("2026-05-18 09:00:00");
        telemetry.setDoorOpen(false);
        telemetry.setRemainingKm(15.0);
        telemetry.setOutsideTemp(26.0);
        req.setLatestTelemetry(telemetry);

        req.setTelemetryHistory(history != null ? history : List.of());
        req.setAlerts(List.of());

        return req;
    }

    private List<EvaluateRequest.HistoryPayload> buildHistory(double... temps) {
        List<EvaluateRequest.HistoryPayload> list = new ArrayList<>();
        for (int i = 0; i < temps.length; i++) {
            EvaluateRequest.HistoryPayload hp = new EvaluateRequest.HistoryPayload();
            hp.setRecordTime("2026-05-18 09:" + String.format("%02d", i * 5) + ":00");
            hp.setTemperature(temps[i]);
            list.add(hp);
        }
        return list;
    }
}
