package com.coldchain.algorithm.model;

import java.util.List;

/**
 * 算法输入请求 — 对应成员B HttpAlgorithmGateway 中定义的 AlgorithmEvaluateRequest
 * 字段名严格按 JSON 小驼峰命名，由 Jackson 自动映射
 */
public class EvaluateRequest {

    private String vehicleCode;
    private VehiclePayload vehicle;
    private TelemetryPayload latestTelemetry;
    private List<HistoryPayload> telemetryHistory;
    private List<AlertPayload> alerts;

    // ===== 内嵌 POJO =====

    public static class VehiclePayload {
        private String vehicleCode;
        private String cargoType;
        private String cargoName;
        private double safeTempMin;
        private double safeTempMax;
        private String status;

        public String getVehicleCode() { return vehicleCode; }
        public void setVehicleCode(String vehicleCode) { this.vehicleCode = vehicleCode; }
        public String getCargoType() { return cargoType; }
        public void setCargoType(String cargoType) { this.cargoType = cargoType; }
        public String getCargoName() { return cargoName; }
        public void setCargoName(String cargoName) { this.cargoName = cargoName; }
        public double getSafeTempMin() { return safeTempMin; }
        public void setSafeTempMin(double safeTempMin) { this.safeTempMin = safeTempMin; }
        public double getSafeTempMax() { return safeTempMax; }
        public void setSafeTempMax(double safeTempMax) { this.safeTempMax = safeTempMax; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class TelemetryPayload {
        private String recordTime;
        private double temperature;
        private double humidity;
        private boolean doorOpen;
        private double speed;
        private double outsideTemp;
        private double lng;
        private double lat;
        private double remainingKm;
        private String trend;

        public String getRecordTime() { return recordTime; }
        public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public double getHumidity() { return humidity; }
        public void setHumidity(double humidity) { this.humidity = humidity; }
        public boolean isDoorOpen() { return doorOpen; }
        public void setDoorOpen(boolean doorOpen) { this.doorOpen = doorOpen; }
        public double getSpeed() { return speed; }
        public void setSpeed(double speed) { this.speed = speed; }
        public double getOutsideTemp() { return outsideTemp; }
        public void setOutsideTemp(double outsideTemp) { this.outsideTemp = outsideTemp; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getRemainingKm() { return remainingKm; }
        public void setRemainingKm(double remainingKm) { this.remainingKm = remainingKm; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }

    public static class HistoryPayload {
        private String recordTime;
        private double temperature;

        public String getRecordTime() { return recordTime; }
        public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
    }

    public static class AlertPayload {
        private String alertId;
        private String level;
        private String alertType;
        private String triggerTime;

        public String getAlertId() { return alertId; }
        public void setAlertId(String alertId) { this.alertId = alertId; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public String getTriggerTime() { return triggerTime; }
        public void setTriggerTime(String triggerTime) { this.triggerTime = triggerTime; }
    }

    // ===== 外层 getter/setter =====

    public String getVehicleCode() { return vehicleCode; }
    public void setVehicleCode(String vehicleCode) { this.vehicleCode = vehicleCode; }
    public VehiclePayload getVehicle() { return vehicle; }
    public void setVehicle(VehiclePayload vehicle) { this.vehicle = vehicle; }
    public TelemetryPayload getLatestTelemetry() { return latestTelemetry; }
    public void setLatestTelemetry(TelemetryPayload latestTelemetry) { this.latestTelemetry = latestTelemetry; }
    public List<HistoryPayload> getTelemetryHistory() { return telemetryHistory; }
    public void setTelemetryHistory(List<HistoryPayload> telemetryHistory) { this.telemetryHistory = telemetryHistory; }
    public List<AlertPayload> getAlerts() { return alerts; }
    public void setAlerts(List<AlertPayload> alerts) { this.alerts = alerts; }
}
