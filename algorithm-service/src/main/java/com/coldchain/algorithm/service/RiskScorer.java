package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.util.List;

/**
 * 风险评分模块 — 参考 Mock 引擎公式，多因子加权评分
 *
 * 公式：
 *   riskScore = (温度 - safeMin) × 8
 *             + min(剩余公里, 30) × 1.2
 *             + (车门开着 ? 12 : 0)
 *             + 告警数 × 8
 *   上限 98 分
 *
 * 映射：
 *   ≥ 80 → HIGH   (高风险)
 *   45-79 → MEDIUM (中风险)
 *   < 45 → LOW    (低风险)
 */
public class RiskScorer {

    private static final double MAX_SCORE = 98.0;
    private static final double HIGH_THRESHOLD = 80.0;
    private static final double MEDIUM_THRESHOLD = 45.0;

    /**
     * 计算风险分数
     */
    public RiskResult calculate(EvaluateRequest request, AnomalyResult anomaly) {
        EvaluateRequest.TelemetryPayload telemetry = request.getLatestTelemetry();
        EvaluateRequest.VehiclePayload vehicle = request.getVehicle();
        List<EvaluateRequest.AlertPayload> alerts = request.getAlerts();

        double temp = telemetry.getTemperature();
        double safeMin = vehicle.getSafeTempMin();
        double remainingKm = telemetry.getRemainingKm();
        boolean doorOpen = telemetry.isDoorOpen();
        int alertCount = alerts != null ? alerts.size() : 0;

        // 核心公式
        double score = (temp - safeMin) * 8.0
                     + Math.min(remainingKm, 30) * 1.2
                     + (doorOpen ? 12.0 : 0.0)
                     + alertCount * 8.0;

        // 异常检测加成：如果是 TREND_RISE 类型，额外加成（升温趋势增加风险）
        if (anomaly.isDetected() && "TREND_RISE".equals(anomaly.getType())) {
            score += 5.0;
        }

        // 截断上限
        score = Math.min(score, MAX_SCORE);
        // 不能低于 0
        score = Math.max(score, 0.0);

        // 映射风险等级
        String level, label;
        if (score >= HIGH_THRESHOLD) {
            level = "HIGH";
            label = "高风险";
        } else if (score >= MEDIUM_THRESHOLD) {
            level = "MEDIUM";
            label = "中风险";
        } else {
            level = "LOW";
            label = "低风险";
        }

        return new RiskResult(Math.round(score * 10.0) / 10.0, level, label);
    }
}
