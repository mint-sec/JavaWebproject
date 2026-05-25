package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.util.List;

/**
 * 异常检测模块 — 两种检测方式：
 * 1. 阈值越界检测：温度超出 [safeTempMin, safeTempMax]
 * 2. 滑动窗口趋势检测：最近 6 条记录温度持续上升且超过安全区中点
 */
public class AnomalyDetector {

    /** 滑动窗口大小（条数），5 分钟 × 6 = 30 分钟窗口 */
    private static final int WINDOW_SIZE = 6;

    /**
     * 主入口：对一次请求做异常检测
     */
    public AnomalyResult detect(EvaluateRequest request) {
        EvaluateRequest.VehiclePayload vehicle = request.getVehicle();
        EvaluateRequest.TelemetryPayload latest = request.getLatestTelemetry();
        List<EvaluateRequest.HistoryPayload> history = request.getTelemetryHistory();

        double temp = latest.getTemperature();
        double safeMin = vehicle.getSafeTempMin();
        double safeMax = vehicle.getSafeTempMax();

        // 方法一：阈值越界
        if (temp < safeMin || temp > safeMax) {
            String reason = temp < safeMin
                    ? "温度低于安全下限" + safeMin + "°C"
                    : "温度超过安全上限" + safeMax + "°C";
            return new AnomalyResult(true, "THRESHOLD_BREACH", reason, 0);
        }

        // 方法二：滑动窗口趋势检测
        return detectTrend(latest, history, safeMin, safeMax);
    }

    /**
     * 趋势检测：取最近 WINDOW_SIZE 条记录，算最小二乘斜率
     */
    private AnomalyResult detectTrend(
            EvaluateRequest.TelemetryPayload latest,
            List<EvaluateRequest.HistoryPayload> history,
            double safeMin, double safeMax) {

        // 加入当前点，组成完整序列
        int total = Math.min(history.size() + 1, WINDOW_SIZE);
        if (total < 3) {
            return AnomalyResult.normal();  // 数据太少，无法判断趋势
        }

        double[] x = new double[total];
        double[] y = new double[total];

        // 先放历史数据
        int start = Math.max(0, history.size() - WINDOW_SIZE + 1);
        int idx = 0;
        for (int i = start; i < history.size(); i++) {
            x[idx] = idx;
            y[idx] = history.get(i).getTemperature();
            idx++;
        }
        // 最后一个点是当前温度
        x[idx] = idx;
        y[idx] = latest.getTemperature();

        double slope = calcSlope(x, y);
        double midpoint = (safeMin + safeMax) / 2.0;  // 安全区中点，疫苗场景 = 5.0°C

        if (slope > 0 && latest.getTemperature() > midpoint) {
            // 预测多久后超标
            int minutesToLimit = 0;
            if (slope > 0.001) {
                minutesToLimit = (int) Math.round((safeMax - latest.getTemperature()) / slope * 5);
                if (minutesToLimit < 0) minutesToLimit = 0;
            }

            String reason = "连续升温";
            if (minutesToLimit > 0) {
                reason += "且预计" + minutesToLimit + "分钟后越过上限";
            }
            return new AnomalyResult(true, "TREND_RISE", reason, minutesToLimit);
        }

        return AnomalyResult.normal();
    }

    /**
     * 最小二乘法求斜率
     * @param x  时间序号 [0, 1, 2, ...]
     * @param y  温度值
     * @return   斜率，正数=升温，负数=降温
     */
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
}
