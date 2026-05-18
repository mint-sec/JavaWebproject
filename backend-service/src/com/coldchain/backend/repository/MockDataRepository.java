package com.coldchain.backend.repository;

import com.coldchain.backend.model.AlertRecord;
import com.coldchain.backend.model.TelemetryRecord;
import com.coldchain.backend.model.Vehicle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MockDataRepository {
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<TelemetryRecord> telemetryRecords = new ArrayList<>();
    private final List<AlertRecord> alerts = new ArrayList<>();

    public MockDataRepository() {
        loadMockData();
    }

    public List<Vehicle> findAllVehicles() {
        return List.copyOf(vehicles);
    }

    public TelemetryRecord findLatestTelemetryByVehicleId(String vehicleId) {
        return telemetryRecords.stream()
                .filter(record -> record.getVehicleId().equals(vehicleId))
                .max(Comparator.comparing(TelemetryRecord::getRecordTime))
                .orElse(null);
    }

    public List<AlertRecord> findAlertsByVehicleId(String vehicleId) {
        return alerts.stream()
                .filter(alert -> alert.getVehicleId().equals(vehicleId))
                .collect(Collectors.toList());
    }

    private void loadMockData() {
        vehicles.add(new Vehicle("CC-VA-01", "京A-1024", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
        vehicles.add(new Vehicle("CC-VA-02", "京A-1025", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
        vehicles.add(new Vehicle("CC-VA-03", "京A-1026", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
        vehicles.add(new Vehicle("CC-VA-04", "京A-1027", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
        vehicles.add(new Vehicle("CC-VA-05", "京A-1028", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));

        telemetryRecords.add(new TelemetryRecord("CC-VA-01", "2026-05-18 09:25:00", 7.5, 70.0, false, 35.0, 31.0, 116.397, 39.908, 13.4, "逼近上限"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-02", "2026-05-18 09:25:00", 5.2, 65.0, false, 42.0, 31.0, 116.402, 39.910, 21.1, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-03", "2026-05-18 09:25:00", 6.1, 67.0, true, 18.0, 30.0, 116.410, 39.906, 19.6, "开门波动"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-04", "2026-05-18 09:25:00", 4.8, 62.0, false, 46.0, 29.0, 116.421, 39.912, 25.8, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-05", "2026-05-18 09:25:00", 7.1, 69.0, false, 30.0, 32.0, 116.430, 39.918, 16.7, "连续升温"));

        alerts.add(new AlertRecord("ALT-20260518-001", "CC-VA-01", "HIGH", "高风险临界告警", "疫苗车厢温度接近安全上限，剩余路线较长。", "比较最近冷库改道方案与继续配送方案的综合成本。", "2026-05-18 09:25:00"));
        alerts.add(new AlertRecord("ALT-20260518-002", "CC-VA-01", "MEDIUM", "预测型预警", "若继续当前趋势，未来 12 分钟温度可能突破 8°C。", "优先完成最近高敏货物配送，减少暴露时间。", "2026-05-18 09:20:00"));
        alerts.add(new AlertRecord("ALT-20260518-003", "CC-VA-03", "MEDIUM", "卸货开门温升", "车门开启造成温度快速上升，短时风险增加。", "缩短开门时长，完成站点作业后立即恢复制冷。", "2026-05-18 09:15:00"));
    }
}
