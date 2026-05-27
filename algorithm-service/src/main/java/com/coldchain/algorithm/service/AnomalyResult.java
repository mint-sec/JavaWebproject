package com.coldchain.algorithm.service;

/**
 * 异常检测结果 — AnomalyDetector 的输出
 */
public class AnomalyResult {

    private boolean detected;
    private String type;        // THRESHOLD_BREACH / TREND_RISE / DOOR_EVENT / NONE
    private String reason;
    private int minutesToLimit;
    private double slope;       // 温度变化斜率（供 RiskScorer 使用）

    public AnomalyResult() {}

    public AnomalyResult(boolean detected, String type, String reason, int minutesToLimit) {
        this(detected, type, reason, minutesToLimit, 0);
    }

    public AnomalyResult(boolean detected, String type, String reason, int minutesToLimit, double slope) {
        this.detected = detected;
        this.type = type;
        this.reason = reason;
        this.minutesToLimit = minutesToLimit;
        this.slope = slope;
    }

    /** 工厂方法：无异常 */
    public static AnomalyResult normal() {
        return new AnomalyResult(false, "NONE", "温度正常", 0, 0);
    }

    public boolean isDetected() { return detected; }
    public void setDetected(boolean detected) { this.detected = detected; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public int getMinutesToLimit() { return minutesToLimit; }
    public void setMinutesToLimit(int minutesToLimit) { this.minutesToLimit = minutesToLimit; }
    public double getSlope() { return slope; }
    public void setSlope(double slope) { this.slope = slope; }
}
