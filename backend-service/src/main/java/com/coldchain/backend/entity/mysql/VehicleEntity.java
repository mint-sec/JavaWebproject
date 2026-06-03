package com.coldchain.backend.entity.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class VehicleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_code", nullable = false, unique = true, length = 32)
    private String vehicleCode;

    @Column(name = "plate_number", nullable = false, length = 32)
    private String plateNumber;

    @Column(name = "cargo_type", nullable = false, length = 32)
    private String cargoType;

    @Column(name = "cargo_name", nullable = false, length = 64)
    private String cargoName;

    @Column(name = "safe_temp_min", nullable = false)
    private double safeTempMin;

    @Column(name = "safe_temp_max", nullable = false)
    private double safeTempMax;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(length = 32)
    private String driver;

    @Column(length = 128)
    private String route;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public String getCargoName() {
        return cargoName;
    }

    public void setCargoName(String cargoName) {
        this.cargoName = cargoName;
    }

    public double getSafeTempMin() {
        return safeTempMin;
    }

    public void setSafeTempMin(double safeTempMin) {
        this.safeTempMin = safeTempMin;
    }

    public double getSafeTempMax() {
        return safeTempMax;
    }

    public void setSafeTempMax(double safeTempMax) {
        this.safeTempMax = safeTempMax;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDriver() { return driver; }
    public void setDriver(String driver) { this.driver = driver; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
