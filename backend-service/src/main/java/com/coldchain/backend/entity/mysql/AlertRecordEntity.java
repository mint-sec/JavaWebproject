package com.coldchain.backend.entity.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class AlertRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", nullable = false, unique = true, length = 40)
    private String alertId;

    @Column(name = "vehicle_code", nullable = false, length = 32)
    private String vehicleCode;

    @Column(name = "alert_level", nullable = false, length = 16)
    private String alertLevel;

    @Column(name = "alert_type", nullable = false, length = 32)
    private String alertType;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "detail_text", nullable = false, length = 255)
    private String detailText;

    @Column(name = "suggestion", nullable = false, length = 255)
    private String suggestion;

    @Column(name = "trigger_time", nullable = false)
    private LocalDateTime triggerTime;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(length = 64)
    private String owner;

    @Column(name = "owner_user_id", length = 32)
    private String ownerUserId;

    @Column(name = "process_status", length = 16)
    private String processStatus;

    @Column(length = 255)
    private String note;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "domain", nullable = false, length = 16)
    private String domain;

    public Long getId() {
        return id;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailText() {
        return detailText;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public LocalDateTime getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(LocalDateTime triggerTime) {
        this.triggerTime = triggerTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
}
