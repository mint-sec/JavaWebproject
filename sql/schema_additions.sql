-- =====================================================
-- 新增表和扩展现有表字段
-- 适用数据库：MySQL 8.x (coldchain_mvp)
-- =====================================================

-- 1. 用户表（新增 — 后端认证系统数据）
CREATE TABLE IF NOT EXISTS users (
    pk          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id     VARCHAR(32)     NOT NULL UNIQUE COMMENT '用户编号',
    username    VARCHAR(64)     NOT NULL UNIQUE COMMENT '用户名',
    display_name VARCHAR(64)    NOT NULL COMMENT '显示名称',
    phone       VARCHAR(20)     NOT NULL UNIQUE COMMENT '手机号',
    email       VARCHAR(128)    NOT NULL UNIQUE COMMENT '邮箱',
    password    VARCHAR(256)    NOT NULL COMMENT 'BCrypt 密码',
    role        VARCHAR(16)     NOT NULL DEFAULT 'USER',
    status      VARCHAR(16)     NOT NULL DEFAULT '启用中',
    origin      VARCHAR(32)     NOT NULL DEFAULT '用户注册',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 车辆表扩展（添加管理元数据字段，算法负责生成基础数据）
ALTER TABLE vehicles
    ADD COLUMNdriver      VARCHAR(32)  DEFAULT ''  COMMENT '司机姓名',
    ADD COLUMNroute       VARCHAR(128) DEFAULT ''  COMMENT '路线描述',
    ADD COLUMNupdated_at  DATETIME     NULL        COMMENT '管理后台最后更新时间';

-- 3. 告警表扩展（添加处理流程字段，算法负责生成告警事件）
ALTER TABLE alerts
    ADD COLUMNowner           VARCHAR(64)  DEFAULT ''  COMMENT '处理人',
    ADD COLUMNprocess_status  VARCHAR(16)  DEFAULT '待处理' COMMENT '处理状态：待处理/处理中/已处理',
    ADD COLUMNnote            VARCHAR(255) DEFAULT ''  COMMENT '处理备注',
    ADD COLUMNhandled_at      DATETIME     NULL       COMMENT '处理完成时间';
