package com.coldchain.backend.entity.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_plans")
public class RoutePlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_code", nullable = false, length = 32)
    private String vehicleCode;

    @Column(name = "plan_type", nullable = false, length = 32)
    private String planType;

    @Column(name = "plan_title", nullable = false, length = 128)
    private String planTitle;

    @Column(name = "plan_detail", nullable = false, length = 255)
    private String planDetail;

    @Column(name = "estimated_cost", length = 128)
    private String estimatedCost;

    @Column(name = "estimated_benefit", length = 128)
    private String estimatedBenefit;

    @Column(name = "recommended", nullable = false)
    private boolean recommended;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    public Long getId() {
        return id;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public String getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(String planDetail) {
        this.planDetail = planDetail;
    }

    public String getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(String estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getEstimatedBenefit() {
        return estimatedBenefit;
    }

    public void setEstimatedBenefit(String estimatedBenefit) {
        this.estimatedBenefit = estimatedBenefit;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
