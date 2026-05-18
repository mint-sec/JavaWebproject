package com.coldchain.backend.model;

import com.coldchain.backend.util.JsonUtil;

public class TelemetryRecord {
    private final String vehicleId;
    private final String recordTime;
    private final double temperature;
    private final double humidity;
    private final boolean doorOpen;
    private final double speed;
    private final double outsideTemp;
    private final double lng;
    private final double lat;
    private final double remainingKm;
    private final String trend;

    public TelemetryRecord(
            String vehicleId,
            String recordTime,
            double temperature,
            double humidity,
            boolean doorOpen,
            double speed,
            double outsideTemp,
            double lng,
            double lat,
            double remainingKm,
            String trend) {
        this.vehicleId = vehicleId;
        this.recordTime = recordTime;
        this.temperature = temperature;
        this.humidity = humidity;
        this.doorOpen = doorOpen;
        this.speed = speed;
        this.outsideTemp = outsideTemp;
        this.lng = lng;
        this.lat = lat;
        this.remainingKm = remainingKm;
        this.trend = trend;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public String toJson() {
        return "{"
                + "\"vehicleId\":\"" + JsonUtil.escape(vehicleId) + "\","
                + "\"recordTime\":\"" + JsonUtil.escape(recordTime) + "\","
                + "\"temperature\":" + JsonUtil.formatDouble(temperature) + ","
                + "\"humidity\":" + JsonUtil.formatDouble(humidity) + ","
                + "\"doorOpen\":" + doorOpen + ","
                + "\"speed\":" + JsonUtil.formatDouble(speed) + ","
                + "\"outsideTemp\":" + JsonUtil.formatDouble(outsideTemp) + ","
                + "\"lng\":" + JsonUtil.formatDouble(lng) + ","
                + "\"lat\":" + JsonUtil.formatDouble(lat) + ","
                + "\"remainingKm\":" + JsonUtil.formatDouble(remainingKm) + ","
                + "\"trend\":\"" + JsonUtil.escape(trend) + "\""
                + "}";
    }
}
