USE coldchain_mvp;

DELETE FROM operation_logs;
DELETE FROM login_logs;
DELETE FROM route_plans;
DELETE FROM risk_assessments;
DELETE FROM alerts;
DELETE FROM telemetry_records;
DELETE FROM vehicles;
DELETE FROM users;
DELETE FROM cargo_profiles;

INSERT INTO users (user_id, username, display_name, phone, email, password, role, status, origin, created_at, login_failure_count, locked_until)
VALUES
('USR-ADMIN-001', 'admin', 'admin', '13800000001', 'admin@coldchain.local', '$2a$10$VnpHXL/PZfRSfzg9HXCz3eUVc/vA85oHjWw11U0jk3EbcFpovDCMS', 'ADMIN', '启用中', '系统账号', '2026-06-03 09:00:00', 0, NULL),
('USR-OPS-001', 'operator', 'operator', '13800000002', 'operator@coldchain.local', '$2a$10$O/4uUUGmYdYEBFmT3ozfHeVtEJ20hdaJ4d5Emds4lVQfJsqWDonYC', 'USER', '启用中', '系统账号', '2026-06-03 09:00:00', 0, NULL);

INSERT INTO cargo_profiles (cargo_type, cargo_name, safe_temp_min, safe_temp_max, sensitivity_level, description)
VALUES
('VACCINE', '疫苗', 2.00, 8.00, 'HIGH', '疫苗冷链运输主场景');

INSERT INTO vehicles (vehicle_code, display_code, plate_number, cargo_type, cargo_name, safe_temp_min, safe_temp_max, status, owner_user_id, driver, route, route_distance_km, created_at, updated_at)
VALUES
('CC-VA-01', 'CC-VA-01', '京A-1024', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT', 'USR-ADMIN-001', '刘鹏', '北京仓库 -> 市医院', 50.00, '2026-06-03 09:00:00', '2026-06-03 09:25:00'),
('CC-VA-02', 'CC-VA-02', '京A-1025', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT', 'USR-ADMIN-001', '王杰', '区域仓库 -> 门诊 A', 28.00, '2026-06-03 09:00:00', '2026-06-03 09:10:00'),
('CC-VA-03', 'CC-VA-03', '京A-1026', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT', 'USR-ADMIN-001', '陈宇', '北区冷库 -> 疾控中心', 24.00, '2026-06-03 09:00:00', '2026-06-03 08:40:00'),
('CC-VA-04', 'CC-VA-04', '京A-1027', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT', 'USR-ADMIN-001', '赵峰', '中心冷库 -> 社区接种点 A', 31.00, '2026-06-03 09:00:00', '2026-06-03 08:55:00'),
('CC-VA-05', 'CC-VA-05', '京A-1028', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT', 'USR-ADMIN-001', '周凯', '中心冷库 -> 社区接种点 B', 26.00, '2026-06-03 09:00:00', '2026-06-03 09:05:00');

INSERT INTO telemetry_records (vehicle_code, record_time, temperature, humidity, door_open, speed, outside_temp, lng, lat, remaining_km, trend)
VALUES
('CC-VA-01', '2026-06-10 09:00:00', 4.60, 66.00, 0, 42.00, 28.00, 116.360000, 39.900000, 50.00, '温度平稳'),
('CC-VA-02', '2026-06-10 09:00:00', 5.00, 64.00, 0, 40.00, 28.00, 116.390000, 39.906000, 28.00, '温度平稳'),
('CC-VA-03', '2026-06-10 09:00:00', 5.80, 66.00, 0, 36.00, 28.00, 116.398000, 39.902000, 24.00, '温度平稳'),
('CC-VA-04', '2026-06-10 09:00:00', 4.70, 61.00, 0, 45.00, 28.00, 116.408000, 39.907000, 31.00, '温度平稳'),
('CC-VA-05', '2026-06-10 09:00:00', 6.20, 67.00, 0, 34.00, 29.00, 116.418000, 39.913000, 26.00, '趋势关注');

INSERT INTO alerts (alert_id, vehicle_code, alert_level, alert_type, title, detail_text, suggestion, trigger_time, status, owner, owner_user_id, process_status, note, handled_at, domain)
VALUES
('ADM-ALT-01', 'CC-VA-01', 'HIGH', 'TREND_WARNING', '高温风险待处理', '车辆 CC-VA-01 的应急改道方案仍待确认。', '优先检查制冷系统，并评估是否改道冷库。', '2026-06-03 09:25:00', 'OPEN', 'admin', 'USR-ADMIN-001', '待处理', '', NULL, 'BUSINESS'),
('ADM-ALT-02', 'CC-VA-03', 'MEDIUM', 'DOOR_EVENT', '多点卸货波动', '多点配送导致频繁开关门，温控波动增加。', '建议缩短站点停留时间并加强制冷检查。', '2026-06-03 09:18:00', 'OPEN', 'admin', 'USR-ADMIN-001', '处理中', '已联系司机，等待进一步确认。', NULL, 'BUSINESS'),
('ADM-ALT-03', 'CC-VA-05', 'MEDIUM', 'TREND_WARNING', '温升趋势关注', '温度持续升高，已经接近高风险区间。', '建议优先检查制冷负载并缩短停留。', '2026-06-03 09:20:00', 'OPEN', 'admin', 'USR-ADMIN-001', '待处理', '', NULL, 'BUSINESS');

INSERT INTO risk_assessments (vehicle_code, risk_score, risk_level, risk_label, risk_reason, predicted_minutes_to_limit, algorithm_version, algorithm_source, assessment_time)
VALUES
('CC-VA-01', 78.00, 'MEDIUM', '中风险', '温度持续升高，已经接近安全上限。', 15, 'mock-risk-v1', 'MOCK_GATEWAY', '2026-06-03 09:25:00');

INSERT INTO route_plans (vehicle_code, plan_type, plan_title, plan_detail, estimated_cost, estimated_benefit, recommended, created_time)
VALUES
('CC-VA-01', 'PRIORITY_DELIVERY', '优先配送最近站点', '建议优先完成最近高敏站点，减少超温暴露时间。', '小幅调整配送顺序', '减少暴露时长', 1, '2026-06-03 09:25:00');

INSERT INTO login_logs (log_id, account, role_label, result, ip, detail, created_at)
VALUES
('LGN-20260603-001', 'admin', '管理员', '成功', '127.0.0.1', '本地开发环境登录', '2026-06-03 09:08:12'),
('LGN-20260603-002', 'operator', '普通用户', '成功', '127.0.0.1', '业务用户登录工作台', '2026-06-03 09:16:45');

INSERT INTO operation_logs (log_id, module_name, action_name, operator_name, target_name, result, detail, created_at)
VALUES
('OPR-20260603-001', '用户管理', '修改用户角色', 'admin', 'operator', '成功', '已将用户角色调整为普通用户', '2026-06-03 09:22:33');

ALTER TABLE vehicles
    ADD COLUMN display_code VARCHAR(32) NOT NULL DEFAULT '' AFTER vehicle_code;

UPDATE vehicles
SET display_code = vehicle_code
WHERE display_code = '' OR display_code IS NULL;

ALTER TABLE vehicles
    ADD COLUMN route_distance_km DECIMAL(8,2) NOT NULL DEFAULT 30.00 AFTER route;

UPDATE vehicles
SET route_distance_km = 30.00
WHERE route_distance_km IS NULL OR route_distance_km <= 0;

ALTER TABLE vehicles
    ADD UNIQUE INDEX uk_vehicle_owner_display_code (owner_user_id, display_code);
