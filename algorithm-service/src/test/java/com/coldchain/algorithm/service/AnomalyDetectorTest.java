package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 异常检测模块单元测试
 */
class AnomalyDetectorTest {

    private final AnomalyDetector detector = new AnomalyDetector();

    /** 场景1：温度正常 → 不触发异常 */
    @Test
    void shouldReturnNormalWhenTempSafe() {
        EvaluateRequest req = buildRequest(4.6, 2.0, 8.0, buildHistory(4.4, 4.5, 4.3, 4.6, 4.4));
        AnomalyResult result = detector.detect(req);
        assertFalse(result.isDetected());
        assertEquals("NONE", result.getType());
    }

    /** 场景2：温度超标 → 触发 THRESHOLD_BREACH */
    @Test
    void shouldDetectThresholdBreach() {
        EvaluateRequest req = buildRequest(9.2, 2.0, 8.0, List.of());
        AnomalyResult result = detector.detect(req);
        assertTrue(result.isDetected());
        assertEquals("THRESHOLD_BREACH", result.getType());
    }

    /** 场景3：持续升温 + 超过中点 → 触发 TREND_RISE */
    @Test
    void shouldDetectTrendRise() {
        // 模拟 CC-VA-01 的升温序列
        EvaluateRequest req = buildRequest(7.5, 2.0, 8.0,
                buildHistory(4.6, 4.9, 5.3, 6.1, 6.8));
        AnomalyResult result = detector.detect(req);
        assertTrue(result.isDetected());
        assertEquals("TREND_RISE", result.getType());
        assertTrue(result.getMinutesToLimit() > 0);
    }

    /** 场景4：温度低于下限 → 触发 THRESHOLD_BREACH */
    @Test
    void shouldDetectLowTemp() {
        EvaluateRequest req = buildRequest(1.5, 2.0, 8.0, List.of());
        AnomalyResult result = detector.detect(req);
        assertTrue(result.isDetected());
        assertEquals("THRESHOLD_BREACH", result.getType());
    }

    /** 场景5：斜率计算验证 */
    @Test
    void shouldCalcSlopeCorrectly() {
        double[] x = {0, 1, 2, 3, 4, 5};
        double[] y = {4.6, 4.9, 5.3, 6.1, 6.8, 7.5};
        double slope = AnomalyDetector.calcSlope(x, y);
        // CC-VA-01 升温趋势 ≈ 0.6°C/5分钟
        assertTrue(slope > 0.5, "斜率应 > 0.5，实际=" + slope);
    }

    // ===== 辅助方法 =====

    private EvaluateRequest buildRequest(double temp, double safeMin, double safeMax,
                                         List<EvaluateRequest.HistoryPayload> history) {
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
