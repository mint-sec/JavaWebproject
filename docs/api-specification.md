# 接口规范

## 约定

| 项目 | 规范 |
|---|---|
| 基础路径 | `http://localhost:18081` |
| 接口前缀 | `/api/v1` |
| 鉴权方式 | `Authorization: Bearer {token}`（login/register 除外） |
| 请求格式 | `application/json` |
| 响应格式 | `{ success: boolean, message: string, data: object/array/null }` |
| 时间格式 | `yyyy-MM-dd HH:mm:ss` |
| 字段命名 | 小驼峰 (camelCase) |
| 温度单位 | 摄氏度 (°C) |
| 速度单位 | km/h |
| 里程单位 | km |
| 经纬度 | `lng`, `lat` |

### 统一响应示例

```json
{
  "success": true,
  "message": "ok",
  "data": { ... }
}
```

失败：
```json
{
  "success": false,
  "message": "用户名或密码错误",
  "data": null
}
```

## 枚举字典

### 用户角色
| 枚举值 | 中文 |
|---|---|
| `ADMIN` | 管理员 |
| `USER` | 普通用户 |

### 用户状态
| 枚举值 | 中文 |
|---|---|
| `启用中` | 正常 |
| `已封禁` | 封禁 |

### 车辆状态
| 枚举值 | 中文 |
|---|---|
| `IN_TRANSIT` | 运输中 |
| `IDLE` | 待命中 |
| `MAINTENANCE` | 维修中 |
| `OFFLINE` | 已停用 |

### 告警等级
| 枚举值 | 中文 | 颜色 |
|---|---|---|
| `LOW` | 低风险 | 绿色 |
| `MEDIUM` | 中风险 | 橙色 |
| `HIGH` | 高风险 | 红色 |

### 告警类型
| 枚举值 | 中文 |
|---|---|
| `TREND_WARNING` | 趋势预警 |
| `PREDICTION_WARNING` | 预测预警 |
| `DOOR_EVENT` | 车门事件 |

### 异常类型（算法检测结果）
| 枚举值 | 中文 | 说明 |
|---|---|---|
| `THRESHOLD_BREACH` | 阈值越界 | 温度直接超出安全区间，立即报告 |
| `TREND_RISE` | 趋势上升 | 窗口内持续升温，需连续确认后报告 |
| `DOOR_EVENT` | 车门事件 | 开门时外部高温导致热量灌入，立即报告 |
| `NONE` | 无异常 | 温控正常 |

### 算法来源
| 枚举值 | 说明 |
|---|---|
| `JAVA_HTTP` | 远程 Java 算法服务（端口 5001） |
| `MOCK` | 内置 Mock 规则引擎 |

### 算法版本
| 版本 | 说明 |
|---|---|
| `risk-v2` | 当前版本，含 6 因子评分 + 7 场景路径建议 |
| `risk-v3` | 在 risk-v2 基础上新增趋势型防误报机制 |

### 告警处理状态
| 枚举值 | 中文 |
|---|---|
| `待处理` | 待处理 |
| `处理中` | 处理中 |
| `已处理` | 已处理 |

### 风险等级
| 枚举值 | 中文 |
|---|---|
| `LOW` | 低风险 |
| `MEDIUM` | 中风险 |
| `HIGH` | 高风险 |

### 服务监控色调
| 枚举值 | 说明 |
|---|---|
| `normal` | 正常 |
| `warning` | 观察中 |
| `pending` | 待联调 |
| `danger` | 异常 |

## 接口清单

### 认证

| Method | Path | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/v1/auth/login` | 否 | 用户登录 |
| POST | `/api/v1/auth/register` | 否 | 用户注册（注册后自动登录） |
| GET | `/api/v1/auth/me` | 是 | 获取当前登录用户 |
| POST | `/api/v1/auth/logout` | 是 | 退出登录 |

### 监控大屏

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/dashboard/vehicles/{vehicleCode}` | 大屏聚合数据（summary + route + temperatureHistory + alerts + riskAssessment） |

### 车辆与遥测

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/vehicles` | 车辆列表 |
| GET | `/api/v1/vehicles/{vehicleCode}/telemetry/latest` | 最新遥测数据 |
| GET | `/api/v1/vehicles/{vehicleCode}/telemetry/history?minutes=30` | 历史温度序列 |

### 告警

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/alerts?vehicleCode=&page=&pageSize=` | 分页告警列表 |
| GET | `/api/v1/vehicles/{vehicleCode}/alerts?limit=4` | 车辆最近告警 |
| GET | `/api/v1/alerts/{alertId}` | 告警详情 |
| GET | `/api/v1/vehicles/{vehicleCode}/alerts/summary` | 告警汇总（数量、最高等级、最新标题） |

### 风险评估

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/vehicles/{vehicleCode}/risk-assessments/latest` | 最新风险评估 |
| GET | `/api/v1/vehicles/{vehicleCode}/risk-assessments/history?limit=` | 风险评估历史 |

### 路径建议

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/vehicles/{vehicleCode}/route-plans/latest` | 最新推荐方案 |
| GET | `/api/v1/vehicles/{vehicleCode}/route-plans?limit=` | 候选方案列表 |

### 管理后台

| Method | Path | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/v1/admin/console` | ADMIN | 管理后台聚合数据（概览 + 用户管理 + 日志 + 服务监控） |
| PATCH | `/api/v1/admin/users/{userId}` | ADMIN | 修改用户角色/状态 |

### 业务工作区

| Method | Path | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/v1/workspace/console` | 是 | 工作区聚合数据（我的车辆 + 我的告警） |
| POST | `/api/v1/workspace/vehicles` | 是 | 新增我的车辆 |
| PUT | `/api/v1/workspace/vehicles/{vehicleId}` | 是 | 编辑我的车辆 |
| DELETE | `/api/v1/workspace/vehicles/{vehicleId}` | 是 | 删除我的车辆 |
| PATCH | `/api/v1/workspace/alerts/{alertId}` | 是 | 更新告警处理状态 |

### 仿真同步（仅 mysql profile）

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/simulation/import-telemetry` | 从算法服务拉取遥测入库 |
| POST | `/api/v1/simulation/import-analysis` | 逐车调用 evaluate 并落库 |

### 系统

| Method | Path | 说明 |
|---|---|---|
| GET | `/` | 首页 |
| GET | `/health` | 健康检查 |
| GET | `/api-docs` | 接口文档页 |
| GET | `/api/v1/algorithm/status` | 算法网关状态 |

## 认证接口详情

### POST /api/v1/auth/login

```json
// 请求
{ "account": "admin", "password": "Admin123!" }

// 响应 data
{
  "userId": "USR-ADMIN-001",
  "username": "admin",
  "displayName": "admin",
  "role": "ADMIN",
  "roleLabel": "管理员",
  "token": "eyJhbGciOi...",
  "loggedInAt": "2026-06-10 10:00:00"
}
```

失败场景：用户名不存在、密码错误、用户已封禁 → 401/403

### POST /api/v1/auth/register

```json
// 请求
{
  "username": "zhangsan",
  "phone": "13800000003",
  "email": "zhangsan@example.com",
  "password": "123456",
  "confirmPassword": "123456"
}
```

字段要求：username/phone/email 唯一，password ≥ 6 位。注册成功自动登录，返回结构同 login。

## 管理后台接口详情

### GET /api/v1/admin/console

一次性返回四块数据：

| 字段 | 类型 | 说明 |
|---|---|---|
| `overviewCards` | array | 平台概览卡片（用户数/管理员数/登录记录/事项/监控关注） |
| `users` | array | 用户管理表格数据源 |
| `loginLogs` | array | 登录日志表格数据源 |
| `operationLogs` | array | 操作日志表格数据源 |
| `serviceMonitors` | array | 服务监控卡片（frontend/backend/algorithm 三项） |

### PATCH /api/v1/admin/users/{userId}

```json
// 请求
{ "role": "ADMIN", "status": "启用中" }

// 响应 data（更新后的 User 对象）
```

如果修改的是当前登录用户本人，前端后续刷新登录态。如果修改导致用户被封禁，后续请求返回 403。

## 业务工作区接口详情

### GET /api/v1/workspace/console

返回当前用户自己的车辆和告警：

| 字段 | 说明 |
|---|---|
| `overviewCards` | 我的车辆数 / 待处理告警 / 处理中 / 已处理 |
| `vehicles` | 仅限当前用户名下车辆 |
| `alerts` | 仅限当前用户负责的业务告警，不含平台事项 |

### POST /api/v1/workspace/vehicles

```json
// 请求
{
  "vehicleId": "CC-VA-08",
  "cargoName": "疫苗",
  "status": "运输中",
  "driver": "李明",
  "route": "北京仓库 -> 区域医院"
}
```

`ownerUserId` 和 `ownerName` 由后端从当前登录用户自动补齐。

### PATCH /api/v1/workspace/alerts/{alertId}

```json
// 请求
{
  "owner": "admin",
  "level": "HIGH",
  "status": "处理中",
  "note": "已联系司机，等待进一步确认"
}
```

当 `status=已处理` 时，后端自动补齐 `handledAt`。若告警不属于当前用户，返回 403。`domain=PLATFORM` 的告警不允许通过此接口处理。

## 核心数据结构

### Session
```json
{
  "userId": "USR-ADMIN-001",
  "username": "admin",
  "displayName": "admin",
  "role": "ADMIN",
  "roleLabel": "管理员",
  "token": "eyJhbGciOi...",
  "loggedInAt": "2026-06-10 10:00:00"
}
```

### User
```json
{
  "id": "USR-OPS-001",
  "username": "operator",
  "displayName": "operator",
  "phone": "13800000002",
  "email": "operator@coldchain.local",
  "role": "USER",
  "roleLabel": "普通用户",
  "status": "启用中",
  "origin": "系统账号"
}
```

### Vehicle（工作区）
```json
{
  "vehicleId": "CC-VA-01",
  "cargoName": "疫苗",
  "status": "运输中",
  "driver": "刘鹏",
  "route": "北京仓库 -> 市医院",
  "ownerUserId": "USR-ADMIN-001",
  "ownerName": "admin",
  "updatedAt": "2026-06-10 09:25:00"
}
```

### Alert（工作区）
```json
{
  "id": "ADM-ALT-01",
  "title": "高温风险待处理",
  "level": "HIGH",
  "levelLabel": "高",
  "detail": "车辆 CC-VA-01 的应急改道方案仍待管理员确认。",
  "vehicleId": "CC-VA-01",
  "owner": "admin",
  "ownerUserId": "USR-ADMIN-001",
  "status": "待处理",
  "note": "",
  "handledAt": "",
  "domain": "BUSINESS"
}
```

### ServiceMonitor
```json
{
  "id": "service-backend",
  "name": "后端接口服务",
  "status": "正常",
  "tone": "normal",
  "latency": "25 ms",
  "source": "/api/v1",
  "checkedAt": "2026-06-10 10:10:00",
  "detail": "认证、工作台、监控面板和后台管理接口已统一由 Spring Boot 提供。"
}
```

### LoginLog
```json
{
  "id": "LGN-20260610-001",
  "account": "admin",
  "roleLabel": "管理员",
  "result": "成功",
  "ip": "127.0.0.1",
  "detail": "本地开发环境登录",
  "time": "2026-06-10 09:08:12"
}
```

### OperationLog
```json
{
  "id": "OPR-20260610-001",
  "module": "用户管理",
  "action": "修改用户角色",
  "operator": "admin",
  "target": "operator",
  "result": "成功",
  "detail": "将用户角色调整为普通用户",
  "time": "2026-06-10 09:22:33"
}
```

## 字段命名约定

| 字段 | 类型 | 说明 | 禁止混用 |
|---|---|---|---|
| `vehicleCode` | string | 车辆业务编号，如 `CC-VA-01` | 不要用 `vehicleId` |
| `doorOpen` | boolean | 车门是否开启 | 不要用 0/1 或 `doorStatus` |
| `lng` / `lat` | number | 经纬度 | 固定使用这两个字段名 |
| `triggerTime` / `recordTime` | string | 时间字段 | 不要用时间戳 |
| `riskLevel` / `riskScore` | string/number | 风险等级和分数 | 前后端对齐 |
