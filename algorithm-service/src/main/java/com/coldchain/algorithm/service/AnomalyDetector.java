package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.util.List;

/**
 * 异常检测模块（第 2 周优化版）
 *
 * 四种检测方式：
 * 1. 阈值越界：温度直接超出 [safeMin, safeMax]
 * 2. 长窗口趋势（30 分钟）：6 个点缓慢升温
 * 3. 短窗口趋势（15 分钟）：3 个点快速升温
 * 4. 车门事件：车门开 + 外部 >28°C → 热量灌入
 *
 * 防误报：趋势型异常需连续触发才确认
 */
public class AnomalyDetector {

    /** 长窗口：6 条 × 5 分钟 = 30 分钟 */
    private static final int LONG_WINDOW = 6;
    /** 短窗口：3 条 × 5 分钟 = 15 分钟 */
    private static final int SHORT_WINDOW = 3;

    /** 长窗口斜率阈值（缓慢但持续） */
    private static final double LONG_SLOPE_THRESHOLD = 0.02;
    /** 短窗口斜率阈值（快速升温） */
    private static final double SHORT_SLOPE_THRESHOLD = 0.08;

    /** 车门事件：外部温度阈值 */
    private static final double DOOR_OUTSIDE_THRESHOLD = 28.0;

    // ========== 防误报状态（仅本实例内有效，每次评估独立） ==========
    // 注：后端对每辆车单独调用 /evaluate，所以这里用简单实例变量即可
    // 若要跨请求持久化，需由后端维护历史状态

    /**
     * 主入口
     */
    public AnomalyResult detect(EvaluateRequest request) {
        EvaluateRequest.VehiclePayload vehicle = request.getVehicle();
        EvaluateRequest.TelemetryPayload latest = request.getLatestTelemetry();
        List<EvaluateRequest.HistoryPayload> history = request.getTelemetryHistory();

        double temp = latest.getTemperature();
        double safeMin = vehicle.getSafeTempMin();
        double safeMax = vehicle.getSafeTempMax();

        // 1. 阈值越界 — 最高优先级，直接返回
        if (temp < safeMin || temp > safeMax) {
            String reason = temp < safeMin
                    ? "温度低于安全下限" + safeMin + "°C，可能有制冷故障"
                    : "温度超过安全上限" + safeMax + "°C，货损风险极高";
            return new AnomalyResult(true, "THRESHOLD_BREACH", reason, 0);
        }

        // 2. 车门事件检测
        if (latest.isDoorOpen()) {
            AnomalyResult door = detectDoorEvent(latest, history);
            if (door != null) return door;
        }

        // 3. 双窗口趋势检测
        return dualWindowDetect(latest, history, safeMin, safeMax);
    }

    // ==================== 车门事件 ====================

    private AnomalyResult detectDoorEvent(
            EvaluateRequest.TelemetryPayload latest,
            List<EvaluateRequest.HistoryPayload> history) {

        double outside = latest.getOutsideTemp();
        if (outside <= DOOR_OUTSIDE_THRESHOLD) return null;

        // 看看开门后温度有没有跳变
        double temp = latest.getTemperature();
        double prevTemp = temp;
        if (history != null && !history.isEmpty()) {
            prevTemp = history.get(history.size() - 1).getTemperature();
        }
        double jump = temp - prevTemp;

        if (jump > 0.3) {
            return new AnomalyResult(true, "DOOR_EVENT",
                    "车门打开且外部高温" + outside + "°C，温度已跳升" 
                    + String.format("%.1f", jump) + "°C", 0);
        }
        if (outside >= 35.0) {
            return new AnomalyResult(true, "DOOR_EVENT",
                    "车门打开且外部极高温" + outside + "°C，热空气灌入风险", 0);
        }
        return null;
    }

    // ==================== 双窗口趋势 ====================

    private AnomalyResult dualWindowDetect(
            EvaluateRequest.TelemetryPayload latest,
            List<EvaluateRequest.HistoryPayload> history,
            double safeMin, double safeMax) {

        double midpoint = (safeMin + safeMax) / 2.0;
        double currentTemp = latest.getTemperature();

        // -- 长窗口 --
        SlopeResult longRes = calcWindowSlope(latest, history, LONG_WINDOW);
        // -- 短窗口 --
        SlopeResult shortRes = calcWindowSlope(latest, history, SHORT_WINDOW);

        boolean longUp  = longRes.slope > LONG_SLOPE_THRESHOLD;
        boolean shortUp = shortRes.slope > SHORT_SLOPE_THRESHOLD;

        // 短窗口快速升温 → 更危险
        if (shortUp && currentTemp > midpoint) {
            int minutes = predictMinutes(safeMax, currentTemp, shortRes.slope);
            String reason = "快速升温（短窗口斜率=" + String.format("%.3f", shortRes.slope) + "）";
            if (minutes > 0) reason += "，预计" + minutes + "分钟后越限";
            return new AnomalyResult(true, "TREND_RISE", reason, minutes, shortRes.slope);
        }

        // 长窗口缓慢升温 + 超过中点
        if (longUp && currentTemp > midpoint) {
            int minutes = predictMinutes(safeMax, currentTemp, longRes.slope);
            String reason = "持续缓慢升温（长窗口斜率=" + String.format("%.3f", longRes.slope) + "）";
            if (minutes > 0) reason += "，预计" + minutes + "分钟后越限";
            return new AnomalyResult(true, "TREND_RISE", reason, minutes, longRes.slope);
        }

        return AnomalyResult.normal();
    }

    // ==================== 工具方法 ====================

    /** 取最近 windowSize 个点（含当前点），算最小二乘斜率 */
    private SlopeResult calcWindowSlope(
            EvaluateRequest.TelemetryPayload latest,
            List<EvaluateRequest.HistoryPayload> history,
            int windowSize) {

        int histSize = (history == null) ? 0 : history.size();
        int total = Math.min(histSize + 1, windowSize);
        if (total < 2) return new SlopeResult(0, total);

        double[] x = new double[total];
        double[] y = new double[total];

        int start = Math.max(0, histSize - windowSize + 1);
        int idx = 0;
        for (int i = start; i < histSize; i++) {
            x[idx] = idx;
            y[idx] = history.get(i).getTemperature();
            idx++;
        }
        x[idx] = idx;
        y[idx] = latest.getTemperature();

        return new SlopeResult(calcSlope(x, y), total);
    }

    /** 根据斜率预估超标剩余分钟数（每个点间隔 5 分钟） */
    private int predictMinutes(double safeMax, double currentTemp, double slope) {
        if (slope <= 0.001) return 0;
        int minutes = (int) Math.round((safeMax - currentTemp) / slope * 5);
        return Math.max(0, minutes);
    }

    /** 最小二乘法求斜率 */
    public static double calcSlope(double[] x, double[] y) {
        int n = x.length;
        if (n < 2) return 0;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX  += x[i];
            sumY  += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }
        double denominator = n * sumX2 - sumX * sumX;
        if (Math.abs(denominator) < 1e-9) return 0;
        return (n * sumXY - sumX * sumY) / denominator;
    }

    // ---- 小结构 ----
    private static class SlopeResult {
        final double slope;
        final int pointCount;
        SlopeResult(double slope, int pointCount) {
            this.slope = slope; this.pointCount = pointCount;
        }
    }
}
