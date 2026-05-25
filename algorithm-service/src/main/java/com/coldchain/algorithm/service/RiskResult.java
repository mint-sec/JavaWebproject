package com.coldchain.algorithm.service;

/**
 * 风险评分结果 — RiskScorer 的输出
 */
public class RiskResult {

    private double score;
    private String level;
    private String label;

    public RiskResult() {}

    public RiskResult(double score, String level, String label) {
        this.score = score;
        this.level = level;
        this.label = label;
    }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
