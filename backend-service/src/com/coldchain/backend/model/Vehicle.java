package com.coldchain.backend.model;

import com.coldchain.backend.util.JsonUtil;

public class Vehicle {
    private final String vehicleId;
    private final String plateNumber;
    private final String cargoType;
    private final String cargoName;
    private final double safeTempMin;
    private final double safeTempMax;
    private final String status;

    public Vehicle(
            String vehicleId,
            String plateNumber,
            String cargoType,
            String cargoName,
            double safeTempMin,
            double safeTempMax,
            String status) {
        this.vehicleId = vehicleId;
        this.plateNumber = plateNumber;
        this.cargoType = cargoType;
        this.cargoName = cargoName;
        this.safeTempMin = safeTempMin;
        this.safeTempMax = safeTempMax;
        this.status = status;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String toJson() {
        return "{"
                + "\"vehicleId\":\"" + JsonUtil.escape(vehicleId) + "\","
                + "\"plateNumber\":\"" + JsonUtil.escape(plateNumber) + "\","
                + "\"cargoType\":\"" + JsonUtil.escape(cargoType) + "\","
                + "\"cargoName\":\"" + JsonUtil.escape(cargoName) + "\","
                + "\"safeTempMin\":" + JsonUtil.formatDouble(safeTempMin) + ","
                + "\"safeTempMax\":" + JsonUtil.formatDouble(safeTempMax) + ","
                + "\"status\":\"" + JsonUtil.escape(status) + "\""
                + "}";
    }
}
