package com.coldchain.algorithm.data;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 模拟数据生成器 — 为 5 辆车生成遥测数据
 *
 * CC-VA-01：100 个采样点，模拟一趟完整冷链配送
 * 其他 4 辆车：各 10 个正常波动采样点（后续可扩展）
 */
public class DataGenerator {

    // ============================================================
    // CC-VA-01 主演示车 — 100 个采样点
    // 场景：北京出发往东，制冷逐渐失效导致温度持续上升
    // ============================================================

    public static List<EvaluateRequest.TelemetryPayload> generateCcVa01() {

        int totalPoints = 100;                      // 总采样点数
        int intervalMinutes = 5;                    // 每个采样点间隔 5 分钟
        double startLng = 116.380, startLat = 39.900;   // 起点（北京）
        double endLng   = 116.850, endLat   = 39.940;   // 终点

        double initialTemp = 4.6;                   // 起始温度
        double safeMax      = 8.0;                   // 安全上限
        double totalKm      = 50.0;                  // 全程距离（公里）

        // ---- 设计温度曲线：正常 → 缓慢上升 → 快速上升 → 超标 → 回落 ----
        // 分段占比（占 totalPoints 的比例）
        //   normalRatio  → rampRatio  → criticalRatio → recoveryRatio
        int normalStart   = 0;
        int normalEnd     = (int) (totalPoints * 0.25);   // 0~25：正常期
        int rampEnd       = (int) (totalPoints * 0.55);   // 25~55：缓慢升温期
        int criticalEnd   = (int) (totalPoints * 0.80);   // 55~80：快速升温+超标期
        int recoveryEnd   = totalPoints;                  // 80~100：降温恢复期

        // 各阶段温度范围
        double normalMin = 4.2,   normalMax = 5.5;
        double rampMin   = 5.5,   rampMax   = 7.0;
        double criticalMin = 7.0, criticalMax = 9.2;
        double recoveryMin = 5.0, recoveryMax = 7.5;

        // 预先设定几个特殊事件（车门打开、停车休息）
        // 格式：事件发生在第几个采样点
        int[] doorOpenPoints = {12, 30, 48, 68, 88};  // 5 次开门事件
        int[] stopPoints     = {15, 45, 75};           // 3 次停车休息（停 1-2 个点）

        Random rng = new Random(42);  // 固定种子，保证每次生成结果相同

        List<EvaluateRequest.TelemetryPayload> list = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0);

        double prevTemp = initialTemp;
        double lng = startLng, lat = startLat;

        for (int i = 0; i < totalPoints; i++) {

            // --- 1. 计算时间 ---
            LocalTime t = startTime.plusMinutes((long) i * intervalMinutes);

            // --- 2. 计算温度（分段线性 + 随机抖动）---
            double targetTemp;
            if (i < normalEnd) {
                double ratio = (double) (i - normalStart) / (normalEnd - normalStart);
                targetTemp = normalMin + (normalMax - normalMin) * ratio;
            } else if (i < rampEnd) {
                double ratio = (double) (i - normalEnd) / (rampEnd - normalEnd);
                targetTemp = rampMin + (rampMax - rampMin) * ratio;
            } else if (i < criticalEnd) {
                double ratio = (double) (i - rampEnd) / (criticalEnd - rampEnd);
                targetTemp = criticalMin + (criticalMax - criticalMin) * ratio;
            } else {
                double ratio = (double) (i - criticalEnd) / (recoveryEnd - criticalEnd);
                targetTemp = recoveryMax - (recoveryMax - recoveryMin) * ratio;
            }

            // 温度逐渐趋向目标值 + 微小随机抖动
            double temp = prevTemp + (targetTemp - prevTemp) * 0.3 + (rng.nextDouble() - 0.5) * 0.3;
            temp = Math.round(temp * 10.0) / 10.0;
            prevTemp = temp;

            // --- 3. 计算湿度（随温度变化，温度越高制冷越差，湿度可能上升）---
            double humidity = 65.0 + (temp - 4.0) * 1.5 + (rng.nextDouble() - 0.5) * 4;
            humidity = Math.min(Math.max(humidity, 60), 85);
            humidity = Math.round(humidity * 10.0) / 10.0;

            // --- 4. 车门状态 ---
            boolean doorOpen = contains(doorOpenPoints, i);

            // --- 5. 速度 ---
            boolean isStopping = contains(stopPoints, i)
                    || contains(stopPoints, i - 1);  // 停车持续 2 个点
            double speed;
            if (isStopping) {
                speed = 0;
            } else if (doorOpen) {
                speed = 0;  // 开门肯定停车
            } else {
                // 正常行驶：30~55 km/h，高速路段约 50，市区约 35
                double baseSpeed = 40 + (rng.nextDouble() - 0.5) * 15;
                speed = Math.round(baseSpeed * 10.0) / 10.0;
            }

            // --- 6. 剩余里程 ---
            double progress = (double) i / totalPoints;
            double remainingKm = totalKm * (1 - progress);
            remainingKm = Math.round(remainingKm * 10.0) / 10.0;
            if (remainingKm < 0) remainingKm = 0;

            // --- 7. 外部温度（模拟早晨到午后的升温）---
            double outsideTemp = 26.0 + (double) i / totalPoints * 11.0 + (rng.nextDouble() - 0.5) * 2;
            outsideTemp = Math.round(outsideTemp * 10.0) / 10.0;

            // --- 8. 经纬度（线性插值 + 微小偏移）---
            double ratio = (double) i / totalPoints;
            lng = startLng + (endLng - startLng) * ratio + (rng.nextDouble() - 0.5) * 0.005;
            lat = startLat + (endLat - startLat) * ratio + (rng.nextDouble() - 0.5) * 0.005;
            lng = Math.round(lng * 1000.0) / 1000.0;
            lat = Math.round(lat * 1000.0) / 1000.0;

            // --- 9. 趋势标签 ---
            String trend = buildTrendLabel(temp, safeMax);

            // --- 组装 ---
            EvaluateRequest.TelemetryPayload tp = new EvaluateRequest.TelemetryPayload();
            tp.setRecordTime("2026-05-18 " + String.format("%02d:%02d:00", t.getHour(), t.getMinute()));
            tp.setTemperature(temp);
            tp.setHumidity(humidity);
            tp.setDoorOpen(doorOpen);
            tp.setSpeed(speed);
            tp.setOutsideTemp(outsideTemp);
            tp.setLng(lng);
            tp.setLat(lat);
            tp.setRemainingKm(remainingKm);
            tp.setTrend(trend);
            list.add(tp);
        }

        return list;
    }

    // ============================================================
    // 其他 4 辆车 — 各 10 个正常波动采样点
    // ============================================================

    public static List<EvaluateRequest.TelemetryPayload> generateNormalVehicle(double baseTemp) {
        List<EvaluateRequest.TelemetryPayload> list = new ArrayList<>();
        Random rng = new Random((long) (baseTemp * 100));
        LocalTime start = LocalTime.of(9, 0);
        double lng = 116.30, lat = 39.85;

        for (int i = 0; i < 10; i++) {
            LocalTime t = start.plusMinutes(i * 5L);

            EvaluateRequest.TelemetryPayload tp = new EvaluateRequest.TelemetryPayload();
            tp.setRecordTime("2026-05-18 " + String.format("%02d:%02d:00", t.getHour(), t.getMinute()));
            tp.setTemperature(Math.round((baseTemp + rng.nextDouble() * 0.6 - 0.3) * 10.0) / 10.0);
            tp.setHumidity(Math.round((65.0 + rng.nextDouble() * 10) * 10.0) / 10.0);
            tp.setDoorOpen(false);
            tp.setSpeed(Math.round((35.0 + rng.nextDouble() * 15) * 10.0) / 10.0);
            tp.setOutsideTemp(Math.round((28.0 + rng.nextDouble() * 6) * 10.0) / 10.0);
            tp.setLng(Math.round((lng + i * 0.005 + rng.nextDouble() * 0.002) * 1000.0) / 1000.0);
            tp.setLat(Math.round((lat + i * 0.003 + rng.nextDouble() * 0.002) * 1000.0) / 1000.0);
            tp.setRemainingKm(Math.round((20.0 - i * 2.0 + rng.nextDouble() * 2) * 10.0) / 10.0);
            tp.setTrend("正常");
            list.add(tp);
        }
        return list;
    }

    // ============================================================
    // 工具方法
    // ============================================================

    private static boolean contains(int[] arr, int val) {
        for (int v : arr) {
            if (v == val) return true;
        }
        return false;
    }

    private static String buildTrendLabel(double temp, double safeMax) {
        if (temp >= safeMax + 0.5) return "严重超标";
        if (temp >= safeMax)        return "温度超标";
        if (temp >= safeMax - 0.5)  return "逼近上限";
        if (temp >= safeMax - 1.5)  return "趋势预警";
        if (temp >= safeMax - 2.5)  return "趋势关注";
        if (temp >= 5.0)            return "缓慢上升";
        if (temp >= 4.0)            return "正常";
        return "低温注意";
    }
}
