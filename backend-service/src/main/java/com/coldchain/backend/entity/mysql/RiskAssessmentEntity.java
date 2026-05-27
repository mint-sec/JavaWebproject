package com.coldchain.backend.entity.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_code", nullable = false, length = 32)
    private String vehicleCode;

    @Column(name = "risk_score", nullable = false)
    private double riskScore;

    @Column(name = "risk_level", nullable = false, length = 16)
    private String riskLevel;

    @Column(name = "risk_label", nullable = false, length = 16)
    private String riskLabel;

    @Column(name = "risk_reason", nullable = false, length = 255)
    private String riskReason;

    @Column(name = "predicted_minutes_to_limit")
    private Integer predictedMinutesToLimit;

    @Column(name = "algorithm_version", length = 32)
    private String algorithmVersion;

    @Column(name = "algorithm_source", length = 32)
    private String algorithmSource;

    @Column(name = "assessment_time", nullable = false)
    private LocalDateTime assessmentTime;

    public Long getId() {
        return id;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public Integer getPredictedMinutesToLimit() {
        return predictedMinutesToLimit;
    }

    public void setPredictedMinutesToLimit(Integer predictedMinutesToLimit) {
        this.predictedMinutesToLimit = predictedMinutesToLimit;
    }

    public String getAlgorithmVersion() {
        return algorithmVersion;
    }

    public void setAlgorithmVersion(String algorithmVersion) {
        this.algorithmVersion = algorithmVersion;
    }

    public String getAlgorithmSource() {
        return algorithmSource;
    }

    public void setAlgorithmSource(String algorithmSource) {
        this.algorithmSource = algorithmSource;
    }

    public LocalDateTime getAssessmentTime() {
        return assessmentTime;
    }

    public void setAssessmentTime(LocalDateTime assessmentTime) {
        this.assessmentTime = assessmentTime;
    }
}
