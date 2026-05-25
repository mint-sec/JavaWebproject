package com.coldchain.algorithm.model;

import java.util.List;

/**
 * 算法输出响应 — 对应成员B HttpAlgorithmGateway 中定义的 AlgorithmHttpResponse
 * 字段名严格按 JSON 小驼峰命名，由 Jackson 自动序列化
 */
public class EvaluateResponse {

    private boolean anomalyDetected;
    private String anomalyType;
    private String anomalyReason;
    private Integer predictedMinutesToLimit;
    private double riskScore;
    private String riskLevel;
    private String riskLabel;
    private String algorithmVersion;
    private String algorithmSource;
    private List<RecommendationPayload> recommendations;

    public static class RecommendationPayload {
        private String planType;
        private String title;
        private String detail;
        private String estimatedCost;
        private String estimatedBenefit;
        private boolean recommended;

        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
        public String getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(String estimatedCost) { this.estimatedCost = estimatedCost; }
        public String getEstimatedBenefit() { return estimatedBenefit; }
        public void setEstimatedBenefit(String estimatedBenefit) { this.estimatedBenefit = estimatedBenefit; }
        public boolean isRecommended() { return recommended; }
        public void setRecommended(boolean recommended) { this.recommended = recommended; }
    }

    // ===== getter/setter =====

    public boolean isAnomalyDetected() { return anomalyDetected; }
    public void setAnomalyDetected(boolean anomalyDetected) { this.anomalyDetected = anomalyDetected; }
    public String getAnomalyType() { return anomalyType; }
    public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }
    public String getAnomalyReason() { return anomalyReason; }
    public void setAnomalyReason(String anomalyReason) { this.anomalyReason = anomalyReason; }
    public Integer getPredictedMinutesToLimit() { return predictedMinutesToLimit; }
    public void setPredictedMinutesToLimit(Integer predictedMinutesToLimit) { this.predictedMinutesToLimit = predictedMinutesToLimit; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskLabel() { return riskLabel; }
    public void setRiskLabel(String riskLabel) { this.riskLabel = riskLabel; }
    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
    public String getAlgorithmSource() { return algorithmSource; }
    public void setAlgorithmSource(String algorithmSource) { this.algorithmSource = algorithmSource; }
    public List<RecommendationPayload> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationPayload> recommendations) { this.recommendations = recommendations; }
}
