package com.coldchain.backend.repository;

import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.RiskAssessmentRecord;
import com.coldchain.backend.entity.RoutePlanRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class MockDataRepository {
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<TelemetryRecord> telemetryRecords = new ArrayList<>();
    private final List<AlertRecord> alerts = new ArrayList<>();
    private final List<RiskAssessmentRecord> riskAssessments = new ArrayList<>();
    private final List<RoutePlanRecord> routePlans = new ArrayList<>();

    public MockDataRepository() {
        initVehicles();
        initTelemetry();
        initAlerts();
        initRiskAssessments();
        initRoutePlans();
    }

    public List<Vehicle> findAllVehicles() {
        return List.copyOf(vehicles);
    }

    public Optional<Vehicle> findVehicleByCode(String vehicleCode) {
        return vehicles.stream().filter(vehicle -> vehicle.vehicleCode().equals(vehicleCode)).findFirst();
    }

    public Optional<TelemetryRecord> findLatestTelemetryByVehicleCode(String vehicleCode) {
        return telemetryRecords.stream()
                .filter(record -> record.vehicleCode().equals(vehicleCode))
                .max(Comparator.comparing(TelemetryRecord::recordTime));
    }

    public List<TelemetryRecord> findTelemetryHistoryByVehicleCode(String vehicleCode) {
        return telemetryRecords.stream()
                .filter(record -> record.vehicleCode().equals(vehicleCode))
                .sorted(Comparator.comparing(TelemetryRecord::recordTime))
                .collect(Collectors.toList());
    }

    public List<AlertRecord> findAlertsByVehicleCode(String vehicleCode) {
        return alerts.stream()
                .filter(alert -> alert.vehicleCode().equals(vehicleCode))
                .sorted(Comparator.comparing(AlertRecord::triggerTime).reversed())
                .collect(Collectors.toList());
    }

    public Optional<AlertRecord> findAlertById(String alertId) {
        return alerts.stream().filter(alert -> alert.alertId().equals(alertId)).findFirst();
    }

    public List<RiskAssessmentRecord> findRiskAssessmentsByVehicleCode(String vehicleCode) {
        return riskAssessments.stream()
                .filter(record -> record.vehicleCode().equals(vehicleCode))
                .sorted(Comparator.comparing(RiskAssessmentRecord::assessmentTime).reversed())
                .collect(Collectors.toList());
    }

    public Optional<RiskAssessmentRecord> findLatestRiskAssessmentByVehicleCode(String vehicleCode) {
        return riskAssessments.stream()
                .filter(record -> record.vehicleCode().equals(vehicleCode))
                .max(Comparator.comparing(RiskAssessmentRecord::assessmentTime));
    }

    public List<RoutePlanRecord> findRoutePlansByVehicleCode(String vehicleCode) {
        return routePlans.stream()
                .filter(record -> record.vehicleCode().equals(vehicleCode))
                .sorted(Comparator.comparing(RoutePlanRecord::createdTime).reversed())
                .collect(Collectors.toList());
    }

    public Optional<RoutePlanRecord> findLatestRoutePlanByVehicleCode(String vehicleCode) {
        return routePlans.stream()
                .filter(record -> record.vehicleCode().equals(vehicleCode))
                .max(Comparator.comparing(RoutePlanRecord::createdTime));
    }

    private void initVehicles() {
        vehicles.add(new Vehicle(1L, "CC-VA-01", "京A-1024", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
        vehicles.add(new Vehicle(2L, "CC-VA-02", "京A-1025", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
        vehicles.add(new Vehicle(3L, "CC-VA-03", "京A-1026", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
        vehicles.add(new Vehicle(4L, "CC-VA-04", "京A-1027", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
        vehicles.add(new Vehicle(5L, "CC-VA-05", "京A-1028", "VACCINE", "疫苗", 2.0, 8.0, "IN_TRANSIT"));
    }

    private void initTelemetry() {
        telemetryRecords.add(new TelemetryRecord("CC-VA-01", LocalDateTime.of(2026, 5, 18, 9, 0), 4.6, 66.0, false, 48.0, 30.0, 116.360, 39.900, 26.8, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-01", LocalDateTime.of(2026, 5, 18, 9, 5), 4.9, 65.0, false, 46.0, 30.0, 116.372, 39.901, 24.9, "轻微升温"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-01", LocalDateTime.of(2026, 5, 18, 9, 10), 5.4, 66.0, false, 42.0, 30.0, 116.384, 39.903, 22.2, "连续升温"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-01", LocalDateTime.of(2026, 5, 18, 9, 15), 6.1, 68.0, true, 39.0, 30.0, 116.390, 39.905, 20.5, "开门导致升温"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-01", LocalDateTime.of(2026, 5, 18, 9, 20), 6.8, 69.0, false, 37.0, 31.0, 116.395, 39.907, 16.8, "12 分钟后可能越界"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-01", LocalDateTime.of(2026, 5, 18, 9, 25), 7.5, 70.0, false, 35.0, 31.0, 116.397, 39.908, 13.4, "逼近上限"));

        telemetryRecords.add(new TelemetryRecord("CC-VA-02", LocalDateTime.of(2026, 5, 18, 9, 10), 5.0, 64.0, false, 44.0, 30.0, 116.390, 39.906, 24.6, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-02", LocalDateTime.of(2026, 5, 18, 9, 15), 5.1, 64.0, false, 43.0, 30.0, 116.396, 39.908, 23.0, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-02", LocalDateTime.of(2026, 5, 18, 9, 20), 5.1, 65.0, false, 42.0, 31.0, 116.399, 39.909, 22.0, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-02", LocalDateTime.of(2026, 5, 18, 9, 25), 5.2, 65.0, false, 42.0, 31.0, 116.402, 39.910, 21.1, "温度平稳"));

        telemetryRecords.add(new TelemetryRecord("CC-VA-03", LocalDateTime.of(2026, 5, 18, 9, 10), 5.8, 66.0, false, 26.0, 30.0, 116.398, 39.902, 22.8, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-03", LocalDateTime.of(2026, 5, 18, 9, 15), 6.0, 66.0, true, 21.0, 30.0, 116.404, 39.904, 21.0, "开门波动"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-03", LocalDateTime.of(2026, 5, 18, 9, 20), 6.2, 67.0, true, 19.0, 30.0, 116.407, 39.905, 20.1, "开门波动"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-03", LocalDateTime.of(2026, 5, 18, 9, 25), 6.1, 67.0, true, 18.0, 30.0, 116.410, 39.906, 19.6, "开门波动"));

        telemetryRecords.add(new TelemetryRecord("CC-VA-04", LocalDateTime.of(2026, 5, 18, 9, 10), 4.7, 61.0, false, 47.0, 29.0, 116.408, 39.907, 29.2, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-04", LocalDateTime.of(2026, 5, 18, 9, 15), 4.8, 61.0, false, 46.0, 29.0, 116.414, 39.909, 27.5, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-04", LocalDateTime.of(2026, 5, 18, 9, 20), 4.8, 62.0, false, 46.0, 29.0, 116.418, 39.910, 26.4, "温度平稳"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-04", LocalDateTime.of(2026, 5, 18, 9, 25), 4.8, 62.0, false, 46.0, 29.0, 116.421, 39.912, 25.8, "温度平稳"));

        telemetryRecords.add(new TelemetryRecord("CC-VA-05", LocalDateTime.of(2026, 5, 18, 9, 10), 6.6, 67.0, false, 34.0, 31.0, 116.418, 39.913, 20.9, "持续升温"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-05", LocalDateTime.of(2026, 5, 18, 9, 15), 6.8, 68.0, false, 33.0, 31.0, 116.423, 39.915, 19.1, "持续升温"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-05", LocalDateTime.of(2026, 5, 18, 9, 20), 7.0, 68.0, false, 31.0, 32.0, 116.427, 39.916, 17.6, "逼近上限"));
        telemetryRecords.add(new TelemetryRecord("CC-VA-05", LocalDateTime.of(2026, 5, 18, 9, 25), 7.1, 69.0, false, 30.0, 32.0, 116.430, 39.918, 16.7, "连续升温"));
    }

    private void initAlerts() {
        alerts.add(new AlertRecord("ALT-20260518-001", "CC-VA-01", "HIGH", "TREND_WARNING", "高风险临界告警", "疫苗车厢温度接近安全上限，剩余路线较长。", "比较最近冷库改道方案与继续配送方案的综合成本。", LocalDateTime.of(2026, 5, 18, 9, 25), "OPEN"));
        alerts.add(new AlertRecord("ALT-20260518-002", "CC-VA-01", "MEDIUM", "PREDICTION_WARNING", "预测型预警", "若继续当前趋势，未来 12 分钟温度可能突破 8°C。", "优先完成最近高敏货物配送，减少暴露时间。", LocalDateTime.of(2026, 5, 18, 9, 20), "OPEN"));
        alerts.add(new AlertRecord("ALT-20260518-003", "CC-VA-01", "MEDIUM", "DOOR_EVENT", "卸货开门温升", "车门开启造成温度快速上升，短时风险增加。", "缩短开门时长，完成站点作业后立即恢复制冷。", LocalDateTime.of(2026, 5, 18, 9, 15), "OPEN"));
        alerts.add(new AlertRecord("ALT-20260518-021", "CC-VA-02", "LOW", "NORMAL_STATUS", "运输状态正常", "当前温度处于安全区间内，运输状态稳定。", "继续按既定路线配送。", LocalDateTime.of(2026, 5, 18, 9, 25), "OPEN"));
        alerts.add(new AlertRecord("ALT-20260518-004", "CC-VA-03", "MEDIUM", "DOOR_EVENT", "多点卸货波动", "多点配送导致频繁开关门，温度控制波动增加。", "建议缩短站点停留时间并加强制冷检查。", LocalDateTime.of(2026, 5, 18, 9, 18), "OPEN"));
        alerts.add(new AlertRecord("ALT-20260518-041", "CC-VA-04", "LOW", "NORMAL_STATUS", "温控稳定", "当前车辆温度控制稳定，未发现异常趋势。", "继续保持当前配送节奏。", LocalDateTime.of(2026, 5, 18, 9, 25), "OPEN"));
        alerts.add(new AlertRecord("ALT-20260518-051", "CC-VA-05", "MEDIUM", "TREND_WARNING", "温升趋势关注", "温度持续升高，已接近高风险区间。", "建议优先检查制冷负载并缩短停留。", LocalDateTime.of(2026, 5, 18, 9, 20), "OPEN"));
        alerts.add(new AlertRecord("ALT-20260518-052", "CC-VA-05", "HIGH", "PREDICTION_WARNING", "逼近上限预警", "当前温度逼近安全上限，如趋势持续将进入高风险。", "建议尽快完成近端配送或执行备选路线。", LocalDateTime.of(2026, 5, 18, 9, 25), "OPEN"));
    }

    private void initRiskAssessments() {
        riskAssessments.add(new RiskAssessmentRecord("CC-VA-01", 52.0, "MEDIUM", "中风险", "连续升温，需关注制冷与车门状态。", 20, LocalDateTime.of(2026, 5, 18, 9, 15), "mock-risk-v1", "MOCK_GATEWAY"));
        riskAssessments.add(new RiskAssessmentRecord("CC-VA-01", 68.5, "MEDIUM", "中风险", "温度持续升高，预计后续存在越界风险。", 12, LocalDateTime.of(2026, 5, 18, 9, 20), "mock-risk-v1", "MOCK_GATEWAY"));
        riskAssessments.add(new RiskAssessmentRecord("CC-VA-01", 86.5, "HIGH", "高风险", "当前车上为高敏疫苗，剩余路线较长，货损风险高。", 12, LocalDateTime.of(2026, 5, 18, 9, 25), "mock-risk-v1", "MOCK_GATEWAY"));
        riskAssessments.add(new RiskAssessmentRecord("CC-VA-02", 32.0, "LOW", "低风险", "当前温控处于稳定区间，运输风险较低。", null, LocalDateTime.of(2026, 5, 18, 9, 25), "mock-risk-v1", "MOCK_GATEWAY"));
        riskAssessments.add(new RiskAssessmentRecord("CC-VA-03", 49.0, "MEDIUM", "中风险", "开门波动导致短时风险上升。", 25, LocalDateTime.of(2026, 5, 18, 9, 18), "mock-risk-v1", "MOCK_GATEWAY"));
        riskAssessments.add(new RiskAssessmentRecord("CC-VA-04", 28.0, "LOW", "低风险", "温度和速度均较稳定，当前运输状态正常。", null, LocalDateTime.of(2026, 5, 18, 9, 25), "mock-risk-v1", "MOCK_GATEWAY"));
        riskAssessments.add(new RiskAssessmentRecord("CC-VA-05", 78.0, "MEDIUM", "中风险", "温度持续升高，已接近安全上限，需尽快处置。", 15, LocalDateTime.of(2026, 5, 18, 9, 25), "mock-risk-v1", "MOCK_GATEWAY"));
    }

    private void initRoutePlans() {
        routePlans.add(new RoutePlanRecord("CC-VA-01", "PRIORITY_DELIVERY", "优先配送最近医院", "缩短疫苗暴露时间，优先完成最近高敏站点配送。", "调整后续两站顺序", "减少约 18 分钟暴露时间", false, LocalDateTime.of(2026, 5, 18, 9, 24)));
        routePlans.add(new RoutePlanRecord("CC-VA-01", "REROUTE_COLD_STORAGE", "改道最近冷库", "前往 3 公里外冷库进行临时控温，再重新规划后续配送。", "增加 8 分钟运输成本", "可在短时间内恢复控温", true, LocalDateTime.of(2026, 5, 18, 9, 25)));
        routePlans.add(new RoutePlanRecord("CC-VA-02", "FOLLOW_CURRENT_ROUTE", "按原计划配送", "当前温控风险较低，继续按原计划执行。", "无新增成本", "运输效率最优", true, LocalDateTime.of(2026, 5, 18, 9, 25)));
        routePlans.add(new RoutePlanRecord("CC-VA-03", "CHECK_REFRIGERATION", "检查车门与制冷状态", "当前建议先完成车门关闭与制冷检查，再继续原路线。", "无额外路线成本", "避免风险继续升高", true, LocalDateTime.of(2026, 5, 18, 9, 18)));
        routePlans.add(new RoutePlanRecord("CC-VA-04", "FOLLOW_CURRENT_ROUTE", "按原计划配送", "当前车辆状态平稳，可继续按原计划配送。", "无新增成本", "维持当前效率", true, LocalDateTime.of(2026, 5, 18, 9, 25)));
        routePlans.add(new RoutePlanRecord("CC-VA-05", "PRIORITY_DELIVERY", "优先配送最近站点", "建议优先完成最近高敏站点，降低超温暴露时间。", "小幅调整配送顺序", "减少温控风险扩散", true, LocalDateTime.of(2026, 5, 18, 9, 25)));
        routePlans.add(new RoutePlanRecord("CC-VA-05", "REROUTE_COLD_STORAGE", "改道最近冷库", "如温度继续升高，优先前往最近冷库进行临时控温。", "增加约 6 分钟运输成本", "避免进入高风险状态", false, LocalDateTime.of(2026, 5, 18, 9, 24)));
    }
}
