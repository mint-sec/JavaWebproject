package com.coldchain.algorithm.data;

import com.coldchain.algorithm.model.EvaluateRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟数据生成器 — 为 5 辆车生成遥测数据
 * 目前硬编码在代码里（第 3 周可改为读配置文件）
 */
public class DataGenerator {

    /**
     * 生成主演示车 CC-VA-01 的完整升温链路（6 条记录）
     */
    public static List<EvaluateRequest.TelemetryPayload> generateCcVa01() {
        List<EvaluateRequest.TelemetryPayload> list = new ArrayList<>();

        EvaluateRequest.TelemetryPayload tp;

        tp = new EvaluateRequest.TelemetryPayload();
        tp.setRecordTime("2026-05-18 09:00:00");
        tp.setTemperature(4.6); tp.setHumidity(68.0);
        tp.setDoorOpen(false); tp.setSpeed(40.0);
        tp.setOutsideTemp(28.0); tp.setLng(116.380); tp.setLat(39.900);
        tp.setRemainingKm(20.0); tp.setTrend("正常");
        list.add(tp);

        tp = new EvaluateRequest.TelemetryPayload();
        tp.setRecordTime("2026-05-18 09:05:00");
        tp.setTemperature(4.9); tp.setHumidity(69.0);
        tp.setDoorOpen(false); tp.setSpeed(38.0);
        tp.setOutsideTemp(29.0); tp.setLng(116.385); tp.setLat(39.902);
        tp.setRemainingKm(18.5); tp.setTrend("轻微波动");
        list.add(tp);

        tp = new EvaluateRequest.TelemetryPayload();
        tp.setRecordTime("2026-05-18 09:10:00");
        tp.setTemperature(5.3); tp.setHumidity(70.0);
        tp.setDoorOpen(false); tp.setSpeed(36.0);
        tp.setOutsideTemp(29.0); tp.setLng(116.389); tp.setLat(39.904);
        tp.setRemainingKm(17.0); tp.setTrend("缓慢上升");
        list.add(tp);

        tp = new EvaluateRequest.TelemetryPayload();
        tp.setRecordTime("2026-05-18 09:15:00");
        tp.setTemperature(6.1); tp.setHumidity(72.0);
        tp.setDoorOpen(false); tp.setSpeed(35.0);
        tp.setOutsideTemp(30.0); tp.setLng(116.393); tp.setLat(39.906);
        tp.setRemainingKm(15.8); tp.setTrend("趋势关注");
        list.add(tp);

        tp = new EvaluateRequest.TelemetryPayload();
        tp.setRecordTime("2026-05-18 09:20:00");
        tp.setTemperature(6.8); tp.setHumidity(72.0);
        tp.setDoorOpen(false); tp.setSpeed(34.0);
        tp.setOutsideTemp(31.0); tp.setLng(116.395); tp.setLat(39.907);
        tp.setRemainingKm(14.5); tp.setTrend("趋势预警");
        list.add(tp);

        tp = new EvaluateRequest.TelemetryPayload();
        tp.setRecordTime("2026-05-18 09:25:00");
        tp.setTemperature(7.5); tp.setHumidity(70.0);
        tp.setDoorOpen(false); tp.setSpeed(35.0);
        tp.setOutsideTemp(31.0); tp.setLng(116.397); tp.setLat(39.908);
        tp.setRemainingKm(13.4); tp.setTrend("逼近上限");
        list.add(tp);

        return list;
    }

    /**
     * 生成其他 4 辆车的简单数据（各 3 条正常记录）
     */
    public static List<EvaluateRequest.TelemetryPayload> generateNormalVehicle(double baseTemp) {
        List<EvaluateRequest.TelemetryPayload> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            EvaluateRequest.TelemetryPayload tp = new EvaluateRequest.TelemetryPayload();
            tp.setRecordTime("2026-05-18 09:" + String.format("%02d", i * 5) + ":00");
            tp.setTemperature(baseTemp + Math.random() * 0.6 - 0.3);  // ±0.3 随机波动
            tp.setHumidity(65.0 + Math.random() * 10);
            tp.setDoorOpen(false);
            tp.setSpeed(35.0 + Math.random() * 10);
            tp.setOutsideTemp(28.0 + Math.random() * 5);
            tp.setLng(116.3 + Math.random() * 0.2);
            tp.setLat(39.8 + Math.random() * 0.2);
            tp.setRemainingKm(15.0 + Math.random() * 15);
            tp.setTrend("正常");
            list.add(tp);
        }
        return list;
    }
}
