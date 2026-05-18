INSERT INTO cargo_profiles (cargo_type, cargo_name, safe_temp_min, safe_temp_max, sensitivity_level, description)
VALUES
('VACCINE', '疫苗', 2.00, 8.00, 'HIGH', '疫苗冷链主场景'),
('FRESH', '生鲜', 0.00, 4.00, 'MEDIUM', '备用扩展场景'),
('MEDICINE', '药品', 2.00, 10.00, 'HIGH', '备用扩展场景');

INSERT INTO vehicles (vehicle_id, plate_number, cargo_type, cargo_name, safe_temp_min, safe_temp_max, status)
VALUES
('CC-VA-01', '京A-1024', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-02', '京A-1025', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-03', '京A-1026', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-04', '京A-1027', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-05', '京A-1028', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT');

INSERT INTO telemetry_records (vehicle_id, record_time, temperature, humidity, door_open, speed, outside_temp, lng, lat, remaining_km, trend)
VALUES
('CC-VA-01', '2026-05-18 09:25:00', 7.50, 70.00, 0, 35.00, 31.00, 116.397000, 39.908000, 13.40, '逼近上限'),
('CC-VA-02', '2026-05-18 09:25:00', 5.20, 65.00, 0, 42.00, 31.00, 116.402000, 39.910000, 21.10, '温度平稳'),
('CC-VA-03', '2026-05-18 09:25:00', 6.10, 67.00, 1, 18.00, 30.00, 116.410000, 39.906000, 19.60, '开门波动'),
('CC-VA-04', '2026-05-18 09:25:00', 4.80, 62.00, 0, 46.00, 29.00, 116.421000, 39.912000, 25.80, '温度平稳'),
('CC-VA-05', '2026-05-18 09:25:00', 7.10, 69.00, 0, 30.00, 32.00, 116.430000, 39.918000, 16.70, '连续升温');

INSERT INTO alerts (alert_id, vehicle_id, alert_level, alert_type, title, detail_text, suggestion, trigger_time, status)
VALUES
('ALT-20260518-001', 'CC-VA-01', 'HIGH', 'TREND_WARNING', '高风险临界告警', '疫苗车厢温度接近安全上限，剩余路线较长。', '比较最近冷库改道方案与继续配送方案的综合成本。', '2026-05-18 09:25:00', 'OPEN'),
('ALT-20260518-002', 'CC-VA-01', 'MEDIUM', 'PREDICTION_WARNING', '预测型预警', '若继续当前趋势，未来 12 分钟温度可能突破 8°C。', '优先完成最近高敏货物配送，减少暴露时间。', '2026-05-18 09:20:00', 'OPEN'),
('ALT-20260518-003', 'CC-VA-03', 'MEDIUM', 'DOOR_EVENT', '卸货开门温升', '车门开启造成温度快速上升，短时风险增加。', '缩短开门时长，完成站点作业后立即恢复制冷。', '2026-05-18 09:15:00', 'OPEN');
