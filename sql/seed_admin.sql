-- =====================================================
-- 种子数据 — 仅 users 表（车辆/告警数据由算法服务生成）
-- 前提：已执行 schema_additions.sql
-- =====================================================

-- 密码对照：admin=Admin123!, operator=password123, 其余=123456

INSERT INTO users (user_id, username, display_name, phone, email, password, role, status, origin, created_at) VALUES
('USR-ADMIN-001',     'admin',     'admin',     '13800000001', 'admin@coldchain.local',     '$2a$10$CRQ/drQRJn5MTc1ONAaqzumh0mYqNl9yd6ycwiOj5FehqC.6CqaPq', 'ADMIN', '启用中', '系统账号', '2026-06-03 09:00:00'),
('USR-OPS-001',       'operator',  'operator',  '13800000002', 'operator@coldchain.local',  '$2a$10$SioQShtWDjKEAzkvzdEyP.hOuHsSbF.TAz/P6ALqk7a7sYy1lL5ym', 'USER',  '启用中', '系统账号', '2026-06-03 09:00:00'),
('USR-202606030001',  'zhangsan',  'zhangsan',  '13800000003', 'zhangsan@example.com',      '$2a$10$9b9iMh4TGilMfbRPpjC1BuFGE8WrNhqfKYaXhYOpCEBKWR6Lwg5Z6', 'USER',  '启用中', '用户注册', '2026-06-03 09:00:00'),
('USR-202606030002',  'lisi',      'lisi',      '13800000004', 'lisi@example.com',          '$2a$10$9b9iMh4TGilMfbRPpjC1BuFGE8WrNhqfKYaXhYOpCEBKWR6Lwg5Z6', 'USER',  '启用中', '用户注册', '2026-06-03 09:00:00'),
('USR-202606030003',  'wangwu',    'wangwu',    '13800000005', 'wangwu@example.com',        '$2a$10$9b9iMh4TGilMfbRPpjC1BuFGE8WrNhqfKYaXhYOpCEBKWR6Lwg5Z6', 'USER',  '启用中', '用户注册', '2026-06-03 09:00:00'),
('USR-202606030004',  'zhaoliu',   'zhaoliu',   '13800000006', 'zhaoliu@example.com',       '$2a$10$9b9iMh4TGilMfbRPpjC1BuFGE8WrNhqfKYaXhYOpCEBKWR6Lwg5Z6', 'USER',  '已封禁', '用户注册', '2026-06-03 09:00:00'),
('USR-202606030005',  'sunqi',     'sunqi',     '13800000007', 'sunqi@example.com',         '$2a$10$9b9iMh4TGilMfbRPpjC1BuFGE8WrNhqfKYaXhYOpCEBKWR6Lwg5Z6', 'USER',  '启用中', '用户注册', '2026-06-03 09:00:00'),
('USR-202606030006',  'zhouba',    'zhouba',    '13800000008', 'zhouba@example.com',        '$2a$10$9b9iMh4TGilMfbRPpjC1BuFGE8WrNhqfKYaXhYOpCEBKWR6Lwg5Z6', 'USER',  '启用中', '用户注册', '2026-06-03 09:00:00'),
('USR-202606030007',  'wujiu',     'wujiu',     '13800000009', 'wujiu@example.com',         '$2a$10$9b9iMh4TGilMfbRPpjC1BuFGE8WrNhqfKYaXhYOpCEBKWR6Lwg5Z6', 'USER',  '启用中', '用户注册', '2026-06-03 09:00:00'),
('USR-202606030008',  'zhengshi',  'zhengshi',  '13800000010', 'zhengshi@example.com',      '$2a$10$9b9iMh4TGilMfbRPpjC1BuFGE8WrNhqfKYaXhYOpCEBKWR6Lwg5Z6', 'USER',  '启用中', '用户注册', '2026-06-03 09:00:00');
