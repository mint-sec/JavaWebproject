# 数据库设计

## 基础信息

| 项目 | 配置 |
|---|---|
| 数据库类型 | MySQL 8.x |
| 数据库名 | `coldchain_mvp` |
| 字符集 | `utf8mb4` |
| 主机 | `localhost:3306` |
| 用户名 | `coldchain_user` |
| 密码 | `coldchain_dev_123` |
| DDL 文件 | `backend-service/sql/schema.sql` |
| 种子数据 | `backend-service/sql/seed.sql` |

## ER 概览

```
vehicles (车辆)                    cargo_profiles (货物配置)
┌──────────────────┐              ┌──────────────────┐
│ vehicle_code (PK)│              │ cargo_type (PK)  │
│ plate_number     │              │ cargo_name       │
│ cargo_type ──────┼──────────────│ safe_temp_min    │
│ safe_temp_min    │              │ safe_temp_max    │
│ safe_temp_max    │              │ sensitivity_level│
│ status           │              └──────────────────┘
└──┬───────────────┘
   │ 1:N
   ├────────── telemetry_records (遥测时序)
   │           ┌──────────────────┐
   │           │ id (PK)          │
   │           │ vehicle_code (FK)│
   │           │ record_time      │
   │           │ temperature      │
   │           │ humidity         │
   │           │ door_open        │
   │           │ speed            │
   │           │ outside_temp     │
   │           │ lng, lat         │
   │           │ remaining_km     │
   │           │ trend            │
   │           └──────────────────┘
   │
   ├────────── alerts (告警)
   │           ┌──────────────────┐
   │           │ alert_id (PK)    │
   │           │ vehicle_code (FK)│
   │           │ alert_level      │
   │           │ alert_type       │
   │           │ title            │
   │           │ detail_text      │
   │           │ suggestion       │
   │           │ trigger_time     │
   │           │ status           │
   │           └──────────────────┘
   │
   ├────────── risk_assessments (风险评估)
   │           ┌──────────────────┐
   │           │ id (PK)          │
   │           │ vehicle_code (FK)│
   │           │ risk_score       │
   │           │ risk_level       │
   │           │ risk_label       │
   │           │ risk_reason      │
   │           │ predicted_minutes│
   │           │ algorithm_version│
   │           │ algorithm_source │
   │           │ assessment_time  │
   │           └──────────────────┘
   │
   └────────── route_plans (路径建议)
               ┌──────────────────┐
               │ id (PK)          │
               │ vehicle_code (FK)│
               │ plan_type        │
               │ plan_title       │
               │ plan_detail      │
               │ estimated_cost   │
               │ estimated_benefit│
               │ recommended      │
               │ created_time     │
               └──────────────────┘
```

MVP 阶段未设置外键约束，保留开发灵活性。`vehicle_code` 作为业务主键，前后端统一使用。

## 核心表

### vehicles — 车辆基础信息

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | 自增主键 |
| `vehicle_code` | VARCHAR(32) UNIQUE | 业务编号，如 `CC-VA-01` |
| `plate_number` | VARCHAR(32) | 车牌号 |
| `cargo_type` | VARCHAR(32) | 货物类型枚举，`VACCINE` |
| `cargo_name` | VARCHAR(64) | 货物中文名 |
| `safe_temp_min` | DECIMAL(5,2) | 安全温度下限 |
| `safe_temp_max` | DECIMAL(5,2) | 安全温度上限 |
| `status` | VARCHAR(32) | 车辆状态 |

### telemetry_records — 遥测时序

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | 自增主键 |
| `vehicle_code` | VARCHAR(32) | 关联车辆 |
| `record_time` | DATETIME | 采集时间 |
| `temperature` | DECIMAL(5,2) | 车厢温度 °C |
| `humidity` | DECIMAL(5,2) | 车厢湿度 % |
| `door_open` | TINYINT(1) | 车门是否开启 |
| `speed` | DECIMAL(5,1) | 速度 km/h |
| `outside_temp` | DECIMAL(5,2) | 外界温度 °C |
| `lng` | DECIMAL(10,6) | 经度 |
| `lat` | DECIMAL(10,6) | 纬度 |
| `remaining_km` | DECIMAL(6,1) | 剩余里程 km |
| `trend` | VARCHAR(32) | 温度趋势描述 |

前端温度折线图和历史查询主要依赖此表。

### alerts — 告警记录

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | 自增主键 |
| `alert_id` | VARCHAR(64) UNIQUE | 告警业务编号 |
| `vehicle_code` | VARCHAR(32) | 关联车辆 |
| `alert_level` | VARCHAR(16) | HIGH/MEDIUM/LOW |
| `alert_type` | VARCHAR(32) | 告警类型 |
| `title` | VARCHAR(256) | 告警标题 |
| `detail_text` | TEXT | 告警详情 |
| `suggestion` | TEXT | 处置建议 |
| `trigger_time` | DATETIME | 触发时间 |
| `status` | VARCHAR(16) | 处理状态 |

### risk_assessments — 风险评估

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | 自增主键 |
| `vehicle_code` | VARCHAR(32) | 关联车辆 |
| `risk_score` | DECIMAL(5,2) | 风险分数 |
| `risk_level` | VARCHAR(16) | LOW/MEDIUM/HIGH |
| `risk_label` | VARCHAR(16) | 中文风险等级 |
| `risk_reason` | TEXT | 风险原因摘要 |
| `predicted_minutes_to_limit` | INT | 预计超限分钟数 |
| `algorithm_version` | VARCHAR(32) | 算法版本号 |
| `algorithm_source` | VARCHAR(32) | 算法来源 |
| `assessment_time` | DATETIME | 评估时间 |

### route_plans — 路径建议

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | 自增主键 |
| `vehicle_code` | VARCHAR(32) | 关联车辆 |
| `plan_type` | VARCHAR(64) | 方案类型 |
| `plan_title` | VARCHAR(256) | 方案标题 |
| `plan_detail` | TEXT | 方案详情 |
| `estimated_cost` | VARCHAR(256) | 预估代价 |
| `estimated_benefit` | VARCHAR(256) | 预估收益 |
| `recommended` | TINYINT(1) | 是否为推荐方案 |
| `created_time` | DATETIME | 创建时间 |

一辆车可对应多条候选方案，`recommended=1` 为推荐方案。

## 附加表

### cargo_profiles — 货物配置

MVP 阶段仅保留 `VACCINE` 类型，后续可扩展生鲜、药品等场景。

### users — 用户表

JPA Entity `UserEntity`，包含 `username`、`password`（BCrypt 加密）、`phone`、`email`、`role`、`status` 等字段。

### login_logs / operation_logs — 审计日志

管理员后台日志中心的数据来源，记录登录行为和平台操作。

## 初始化

### 建库

```sql
CREATE DATABASE coldchain_mvp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'coldchain_user'@'localhost' IDENTIFIED BY 'coldchain_dev_123';
GRANT ALL PRIVILEGES ON coldchain_mvp.* TO 'coldchain_user'@'localhost';
FLUSH PRIVILEGES;
```

### 导入表和数据

```bash
mysql -u coldchain_user -p coldchain_mvp < backend-service/sql/schema.sql
mysql -u coldchain_user -p coldchain_mvp < backend-service/sql/seed.sql
```

## 数据源模式

| 模式 | 数据来源 | 配置 |
|---|---|---|
| mock | 内存 `MockDataRepository`，预置 5 辆车完整数据 | `app.datasource.mode=mock`（默认） |
| mysql | JPA Repository + MySQL | `app.datasource.mode=mysql`，需配置 `spring.datasource.*` |

## 接口与表映射

| 接口 | 主要依赖表 |
|---|---|
| `GET /api/v1/vehicles` | vehicles |
| `GET /api/v1/dashboard/vehicles/{code}` | vehicles + telemetry_records + alerts + risk_assessments + route_plans |
| `GET /api/v1/vehicles/{code}/telemetry/latest` | telemetry_records |
| `GET /api/v1/vehicles/{code}/telemetry/history` | telemetry_records |
| `GET /api/v1/alerts` | alerts |
| `GET /api/v1/vehicles/{code}/alerts` | alerts |
| `GET /api/v1/vehicles/{code}/risk-assessments/latest` | risk_assessments |
| `GET /api/v1/vehicles/{code}/route-plans/latest` | route_plans |
| `POST /api/v1/simulation/import-telemetry` | telemetry_records (写入) |
| `POST /api/v1/simulation/import-analysis` | risk_assessments + route_plans (写入) |

## 算法与数据库的关系

- **输入来源**：`telemetry_records`（时间序列）+ `vehicles`（货物类型、安全温区）+ `alerts`（已知异常上下文）
- **输出落库**：`risk_assessments`（风险结果）+ `route_plans`（路径建议）
- 算法不直接操作数据库，由后端 `AlgorithmAnalysisSyncService` 负责编排和落库
