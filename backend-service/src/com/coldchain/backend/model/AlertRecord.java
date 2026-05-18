package com.coldchain.backend.model;

import com.coldchain.backend.util.JsonUtil;

public class AlertRecord {
    private final String alertId;
    private final String vehicleId;
    private final String level;
    private final String title;
    private final String detail;
    private final String suggestion;
    private final String triggerTime;

    public AlertRecord(
            String alertId,
            String vehicleId,
            String level,
            String title,
            String detail,
            String suggestion,
            String triggerTime) {
        this.alertId = alertId;
        this.vehicleId = vehicleId;
        this.level = level;
        this.title = title;
        this.detail = detail;
        this.suggestion = suggestion;
        this.triggerTime = triggerTime;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String toJson() {
        return "{"
                + "\"alertId\":\"" + JsonUtil.escape(alertId) + "\","
                + "\"vehicleId\":\"" + JsonUtil.escape(vehicleId) + "\","
                + "\"level\":\"" + JsonUtil.escape(level) + "\","
                + "\"title\":\"" + JsonUtil.escape(title) + "\","
                + "\"detail\":\"" + JsonUtil.escape(detail) + "\","
                + "\"suggestion\":\"" + JsonUtil.escape(suggestion) + "\","
                + "\"triggerTime\":\"" + JsonUtil.escape(triggerTime) + "\""
                + "}";
    }
}
