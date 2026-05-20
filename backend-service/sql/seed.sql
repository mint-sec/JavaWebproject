INSERT INTO cargo_profiles (cargo_type, cargo_name, safe_temp_min, safe_temp_max, sensitivity_level, description)
VALUES
('VACCINE', '疫苗', 2.00, 8.00, 'HIGH', '疫苗冷链主场景');

INSERT INTO vehicles (vehicle_code, plate_number, cargo_type, cargo_name, safe_temp_min, safe_temp_max, status)
VALUES
('CC-VA-01', '京A-1024', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-02', '京A-1025', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-03', '京A-1026', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-04', '京A-1027', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-05', '京A-1028', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT');

INSERT INTO risk_assessments (vehicle_code, risk_score, risk_level, risk_label, risk_reason, predicted_minutes_to_limit, algorithm_version, algorithm_source, assessment_time)
VALUES
('CC-VA-01', 52.00, 'MEDIUM', '中风险', '连续升温，需关注制冷与车门状态。', 20, 'mock-risk-v1', 'MOCK_GATEWAY', '2026-05-18 09:15:00'),
('CC-VA-01', 68.50, 'MEDIUM', '中风险', '温度持续升高，预计后续存在越界风险。', 12, 'mock-risk-v1', 'MOCK_GATEWAY', '2026-05-18 09:20:00'),
('CC-VA-01', 86.50, 'HIGH', '高风险', '当前车上为高敏疫苗，剩余路线较长，货损风险高。', 12, 'mock-risk-v1', 'MOCK_GATEWAY', '2026-05-18 09:25:00'),
('CC-VA-03', 49.00, 'MEDIUM', '中风险', '开门波动导致短时风险上升。', 25, 'mock-risk-v1', 'MOCK_GATEWAY', '2026-05-18 09:18:00');

INSERT INTO route_plans (vehicle_code, plan_type, plan_title, plan_detail, estimated_cost, estimated_benefit, recommended, created_time)
VALUES
('CC-VA-01', 'PRIORITY_DELIVERY', '优先配送最近医院', '缩短疫苗暴露时间，优先完成最近高敏站点配送。', '调整后续两站顺序', '减少约 18 分钟暴露时间', 0, '2026-05-18 09:24:00'),
('CC-VA-01', 'REROUTE_COLD_STORAGE', '改道最近冷库', '前往 3 公里外冷库进行临时控温，再重新规划后续配送。', '增加 8 分钟运输成本', '可在短时间内恢复控温', 1, '2026-05-18 09:25:00'),
('CC-VA-03', 'CHECK_REFRIGERATION', '检查车门与制冷状态', '当前建议先完成车门关闭与制冷检查，再继续原路线。', '无额外路线成本', '避免风险继续升高', 1, '2026-05-18 09:18:00');
