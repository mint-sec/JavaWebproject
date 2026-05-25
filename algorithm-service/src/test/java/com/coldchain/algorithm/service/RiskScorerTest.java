package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 风险评分模块单元测试
 */
class RiskScorerTest {

    private final RiskScorer scorer = new RiskScorer();

    /** 正常温度 → 低风险 */
    @Test
    void shouldReturnLowRiskForNormalTemp() {
        EvaluateRequest req = buildRequest(4.6, false, 20.0, 0);
        AnomalyResult anomaly = AnomalyResult.normal();
        RiskResult result = scorer.calculate(req, anomaly);

        assertTrue(result.getScore() < 45.0, "正常温度分数应 < 45，实际=" + result.getScore());
        assertEquals("LOW", result.getLevel());
    }

    /** 高温接近上限 → 高风险 */
    @Test
    void shouldReturnHighRiskForHighTemp() {
        EvaluateRequest req = buildRequest(7.5, false, 13.4, 2);
        AnomalyResult anomaly = new AnomalyResult(true, "TREND_RISE", "测试", 12);
        RiskResult result = scorer.calculate(req, anomaly);

        assertTrue(result.getScore() >= 80.0, "高温+告警分数应 ≥ 80，实际=" + result.getScore());
        assertEquals("HIGH", result.getLevel());
    }

    /** 车门打开 → 加分 */
    @Test
    void shouldIncreaseScoreWhenDoorOpen() {
        EvaluateRequest reqClosed = buildRequest(5.0, false, 20.0, 0);
        EvaluateRequest reqOpen   = buildRequest(5.0, true,  20.0, 0);
        AnomalyResult anomaly = AnomalyResult.normal();

        double scoreClosed = scorer.calculate(reqClosed, anomaly).getScore();
        double scoreOpen   = scorer.calculate(reqOpen,   anomaly).getScore();

        assertEquals(12.0, scoreOpen - scoreClosed, 0.01, "车门打开应加 12 分");
    }

    /** 分数不超过上限 98 */
    @Test
    void shouldNotExceedMaxScore() {
        EvaluateRequest req = buildRequest(15.0, true, 40.0, 10);
        AnomalyResult anomaly = new AnomalyResult(true, "TREND_RISE", "", 5);
        RiskResult result = scorer.calculate(req, anomaly);

        assertTrue(result.getScore() <= 98.0, "分数不应超过 98，实际=" + result.getScore());
    }

    // ===== 辅助方法 =====

    private EvaluateRequest buildRequest(double temp, boolean doorOpen,
                                         double remainingKm, int alertCount) {
        EvaluateRequest req = new EvaluateRequest();

        EvaluateRequest.VehiclePayload vehicle = new EvaluateRequest.VehiclePayload();
        vehicle.setSafeTempMin(2.0);
        vehicle.setSafeTempMax(8.0);
        req.setVehicle(vehicle);

        EvaluateRequest.TelemetryPayload telemetry = new EvaluateRequest.TelemetryPayload();
        telemetry.setTemperature(temp);
        telemetry.setDoorOpen(doorOpen);
        telemetry.setRemainingKm(remainingKm);
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
