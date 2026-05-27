package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.util.List;

/**
 * 风险评分模块（第 2 周优化版）
 *
 * 公式（6 因子加权）：
 *   f1 = (温度 - safeMin) × 8            ← 温度偏离度
 *   f2 = min(剩余公里, 30) × 1.2          ← 剩余路程风险
 *   f3 = (车门开着 ? 12 : 0)              ← 车门惩罚
 *   f4 = 告警数 × 8                       ← 历史告警积累
 *   f5 = 斜率因子（新增）                   ← 升温越快越危险
 *   f6 = 外部温度因子（新增）               ← 外面越热制冷越吃力
 *
 *   总分 = f1 + f2 + f3 + f4 + f5 + f6，上限 98 分
 *
 * 风险等级映射（不变）：
 *   ≥ 80 → HIGH / 高风险
 *   45-79 → MEDIUM / 中风险
 *   < 45 → LOW / 低风险
 */
public class RiskScorer {

    private static final double MAX_SCORE = 98.0;
    private static final double HIGH_THRESHOLD = 80.0;
    private static final double MEDIUM_THRESHOLD = 45.0;

    // === 权重 ===
    private static final double W_TEMP_DEVIATION   = 8.0;   // 温度偏离度权重
    private static final double W_REMAINING_KM     = 1.2;   // 剩余里程权重
    private static final double W_DOOR_OPEN        = 12.0;  // 车门打开惩罚
    private static final double W_ALERT            = 8.0;   // 每条告警权重
    private static final double W_SLOPE_HIGH       = 12.0;  // 快速升温加成
    private static final double W_SLOPE_MED        = 6.0;   // 缓慢升温加成
    private static final double W_OUTSIDE_EXTREME  = 10.0;  // 极高温加成
    private static final double W_OUTSIDE_HIGH     = 5.0;   // 高温加成

    // === 阈值 ===
    private static final double SLOPE_FAST   = 0.08;   // 快速升温斜率阈值
    private static final double SLOPE_SLOW   = 0.02;   // 缓慢升温斜率阈值
    private static final double OUTSIDE_EXTREME = 35.0; // 外部极高温
    private static final double OUTSIDE_HIGH    = 30.0; // 外部高温

    public RiskResult calculate(EvaluateRequest request, AnomalyResult anomaly) {
        EvaluateRequest.TelemetryPayload t = request.getLatestTelemetry();
        EvaluateRequest.VehiclePayload v = request.getVehicle();
        List<EvaluateRequest.AlertPayload> alerts = request.getAlerts();

        double temp       = t.getTemperature();
        double safeMin    = v.getSafeTempMin();
        double safeMax    = v.getSafeTempMax();
        double remaining  = t.getRemainingKm();
        boolean doorOpen  = t.isDoorOpen();
        int alertCount    = (alerts != null) ? alerts.size() : 0;
        double outside    = t.getOutsideTemp();
        double slope      = anomaly.getSlope();

        // ---- 六个因子 ----

        // f1：温度偏离度
        double f1 = Math.max(0, temp - safeMin) * W_TEMP_DEVIATION;

        // f2：剩余里程风险（越远越危险）
        double f2 = Math.min(remaining, 30) * W_REMAINING_KM;

        // f3：车门惩罚
        double f3 = doorOpen ? W_DOOR_OPEN : 0;

        // f4：历史告警积累
        double f4 = alertCount * W_ALERT;

        // f5：斜率因子（升温速度）
        double f5 = 0;
        if (slope >= SLOPE_FAST) {
            f5 = W_SLOPE_HIGH;
        } else if (slope >= SLOPE_SLOW) {
            f5 = W_SLOPE_MED;
        }

        // f6：外部温度因子
        double f6 = 0;
        if (outside >= OUTSIDE_EXTREME) {
            f6 = W_OUTSIDE_EXTREME;
        } else if (outside >= OUTSIDE_HIGH) {
            f6 = W_OUTSIDE_HIGH;
        }

        // ---- 特殊加成 ----
        double bonus = 0;

        // 阈值越界 → 直接拉满
        if ("THRESHOLD_BREACH".equals(anomaly.getType())) {
            bonus += 15;
        }
        // 车门事件 + 高温 → 额外惩罚
        if ("DOOR_EVENT".equals(anomaly.getType())) {
            bonus += 10;
        }

        // ---- 总分 ----
        double score = f1 + f2 + f3 + f4 + f5 + f6 + bonus;
        score = Math.min(score, MAX_SCORE);
        score = Math.max(score, 0);

        // ---- 映射等级 ----
        String level, label;
        if (score >= HIGH_THRESHOLD) {
            level = "HIGH";   label = "高风险";
        } else if (score >= MEDIUM_THRESHOLD) {
            level = "MEDIUM"; label = "中风险";
        } else {
            level = "LOW";    label = "低风险";
        }

        return new RiskResult(Math.round(score * 10.0) / 10.0, level, label);
    }
}
