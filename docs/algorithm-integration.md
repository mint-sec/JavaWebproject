# 算法对接

## 架构

```
┌──────────────────────────────┐          ┌──────────────────────┐
│     algorithm-service        │ HTTP/JSON│   backend-service    │
│          :5001               │◄────────►│   AlgorithmGateway    │
│                              │          │     │                │
│  ┌────────────────────────┐  │POST /evaluate│ ├─ HttpGateway  │
│  │ ① AnomalyDetector      │  │          │     ├─ MockEngine     │
│  │   四级异常检测           │  │GET /health│     └─ Switchable   │
│  │   + 防误报机制 (risk-v3) │  │          │                      │
│  └───────────┬────────────┘  │GET /simulation/                 │
│              ▼               │  telemetry                      │
│  ┌────────────────────────┐  │                                │
│  │ ② RiskScorer           │  │                                │
│  │   6因子风险评分          │  │                                │
│  └───────────┬────────────┘  │                                │
│              ▼               │                                │
│  ┌────────────────────────┐  │                                │
│  │ ③ RoutePlanner         │  │                                │
│  │   7场景路径建议          │  │                                │
│  └────────────────────────┘  │                                │
└──────────────────────────────┘          └──────────────────────┘
```

三道流水线**串行依赖**：AnomalyDetector → RiskScorer → RoutePlanner，异常检测结果传入风险评分，风险等级传入路径建议。

算法服务是独立进程，后端通过 HTTP/JSON 调用。算法不直接写数据库，后端负责编排输入、持久化输出、暴露接口。

## 网关模式

### 接口定义

```java
public interface AlgorithmGateway {
    AlgorithmEvaluation evaluate(vehicleCode, vehicle, latestTelemetry,
        telemetryHistory, alerts);
    AlgorithmGatewayStatus status();
}
```

所有调用方只依赖接口，不感知底层是 mock 还是 HTTP。

### 模式切换

`application.yml` 中 `app.algorithm.mode` 控制：

| 值 | 行为 |
|---|---|
| `mock` | 走 `MockAlgorithmEngine`，不发起 HTTP |
| `http` | 走 `HttpAlgorithmGateway` 调用远程服务 |
| `http-gateway` | 同 `http`，但 HTTP 失败时若 `fallback-enabled=true` 降级到 mock |

### Mock 引擎规则

`MockAlgorithmEngine` 基于规则评分，不依赖外部服务：

- 温度 ≥ 6.5 或已有告警 ≥ 2 条 → 判定异常
- 风险分数 = 温度偏差分 + 剩余路程分 + 车门开启分 + 告警数量分
- 分数 ≥ 80 → `HIGH`，≥ 45 → `MEDIUM`，否则 `LOW`
- 高风险 → 建议改道冷库或优先配送
- 中风险 → 建议优先配送或检查制冷设备
- 低风险 → 建议按原计划配送

## 算法流水线详情（risk-v2）

`POST /evaluate` 收到遥测数据后依次执行三道流水线，命中即返回。

### 一、异常检测（AnomalyDetector）

采用**四级检测策略**，按优先级从高到低执行，命中即返回：

| 优先级 | 检测类型 | 条件 | 行为 |
|---|------|------|------|
| 1 | `THRESHOLD_BREACH` 阈值越界 | `temp < safeMin` 或 `temp > safeMax` | 立即报告，最高优先级 |
| 2 | `DOOR_EVENT` 车门事件 | `doorOpen=true` 且 `outsideTemp > 28°C` | 立即报告 |
| 3 | `TREND_RISE` 短窗口快速升温 | 最近 3 点（15 分钟）最小二乘斜率 > 0.08，且当前温度超安全中位点 | 触发防误报确认 |
| 4 | `TREND_RISE` 长窗口缓慢升温 | 最近 6 点（30 分钟）最小二乘斜率 > 0.02，且当前温度超安全中位点 | 触发防误报确认 |

**核心参数**：

| 参数 | 值 | 含义 |
|------|-----|------|
| SHORT_WINDOW | 3 点 × 5 分钟 = 15 分钟 | 短窗口采样范围 |
| LONG_WINDOW | 6 点 × 5 分钟 = 30 分钟 | 长窗口采样范围 |
| SHORT_SLOPE_THRESHOLD | 0.08 | 短窗口斜率阈值 |
| LONG_SLOPE_THRESHOLD | 0.02 | 长窗口斜率阈值 |
| DOOR_OUTSIDE_THRESHOLD | 28.0°C | 车门事件外部温度下限 |

**最小二乘斜率公式**（窗口内 n 个点，x 为采样序号 0/1/2…，y 为温度值）：

$$k = \frac{n \sum x_i y_i - \sum x_i \sum y_i}{n \sum x_i^2 - (\sum x_i)^2} \quad (\text{°C/采样点})$$

**超标时间预估**：

$$t_{predict} = \left\lfloor \frac{T_{max} - T_{current}}{k} \times 5 \right\rfloor \quad (\text{分钟})$$

**异常类型**：

| 类型 | 含义 | 响应方式 |
|------|------|---------|
| `THRESHOLD_BREACH` | 温度越界 | 立即报告 |
| `TREND_RISE` | 趋势上升 | 需连续确认 |
| `DOOR_EVENT` | 车门事件 | 立即报告 |
| `NONE` | 无异常 | — |

### 趋势型防误报机制（risk-v3 新增）

此前 `TREND_RISE` 只要一次最小二乘斜率超标就立刻报告，温度传感器的随机噪声（如 0.2°C 抖动）可能导致误报。risk-v3 加入**连续确认计数器**：

- `TREND_RISE` 首次触发时**不立即报告**，只内部记录"趋势预警收集中"
- 连续触发 `TREND_CONFIRM_REQUIRED`（默认 **2**）次后，正式确认异常
- 确认后只要趋势还在就持续报告，无需重新确认
- 温度恢复正常（趋势消失）时，计数器自动清零

**硬事件不受影响**：`THRESHOLD_BREACH` 和 `DOOR_EVENT` 照常立即报告。

可调参数：`AnomalyDetector.java` 第 33 行 `TREND_CONFIRM_REQUIRED`，默认 2。调大更保守（慢响应），调小更敏感。

### 二、风险评分（RiskScorer）

**6因子加权求和**模型，总分上限 98：

$$Score = \min\left(98,\ \sum_{i=1}^{6} f_i + Bonus\right)$$

| 因子 | 名称 | 计算方式 | 权重 |
|------|------|---------|------|
| f1 | 温度偏离度 | max(0, T_curr − T_min) × 8 | 8.0 |
| f2 | 剩余路程风险 | min(剩余km, 30) × 1.2 | 1.2 |
| f3 | 车门惩罚 | 车门打开 ? 12 : 0 | 12.0 |
| f4 | 告警积累 | 历史告警数 × 8 | 8.0 |
| f5 | 斜率因子 | k ≥ 0.08 → 12, k ≥ 0.02 → 6 | 6~12 |
| f6 | 外部温度 | T_out ≥ 35 → 10, T_out ≥ 30 → 5 | 5~10 |

**特殊加成**：触发 `THRESHOLD_BREACH` +15，触发 `DOOR_EVENT` +10。

**风险等级映射**：

| 分数 | 等级 |
|------|------|
| ≥ 80 | `HIGH` 高风险 |
| 45 ~ 79 | `MEDIUM` 中风险 |
| < 45 | `LOW` 低风险 |

### 三、路径建议（RoutePlanner）

结合**风险等级、剩余里程、车门状态、外部温度**综合判断，覆盖 7 种场景：

| 场景 | 风险 | 剩余km | 条件 | 推荐方案 | 备选方案 |
|------|------|--------|------|---------|---------|
| 1 | HIGH | ≤ 5 | — | `PRIORITY_DELIVERY` | — |
| 2 | HIGH | > 5 | — | `REROUTE_COLD_STORAGE` | `PRIORITY_DELIVERY` |
| 3 | MEDIUM | ≤ 8 | — | `PRIORITY_DELIVERY` | — |
| 4 | MEDIUM | > 8 | 开门+>30°C | `CHECK_REFRIGERATION` | `PRIORITY_DELIVERY` |
| 5 | MEDIUM | > 8 | — | `PRIORITY_DELIVERY` | `CHECK_REFRIGERATION` |
| 6 | LOW | — | 开门+>30°C | `CHECK_REFRIGERATION` | — |
| 7 | LOW | — | >35°C | `FOLLOW_CURRENT_ROUTE` | — |
| default | LOW | — | — | `FOLLOW_CURRENT_ROUTE` | — |

**方案类型**：

| 类型 | 含义 | 典型场景 |
|------|------|---------|
| `FOLLOW_CURRENT_ROUTE` | 按原计划 | 温控正常 |
| `PRIORITY_DELIVERY` | 优先配送最近站点 | 中高风险 |
| `REROUTE_COLD_STORAGE` | 改道冷库 | 高风险且路程远 |
| `CHECK_REFRIGERATION` | 检查制冷/关门 | 车门异常或设备隐患 |

每条建议包含字段：`planType`、`title`、`detail`、`estimatedCost`、`estimatedBenefit`、`recommended`。

---

## HTTP 契约

### evaluate — 评估接口

**请求** `POST /evaluate`

```json
{
  "vehicleCode": "CC-VA-01",
  "vehicle": {
    "vehicleCode": "CC-VA-01",
    "cargoType": "VACCINE",
    "cargoName": "疫苗",
    "safeTempMin": 2.0,
    "safeTempMax": 8.0,
    "status": "IN_TRANSIT"
  },
  "latestTelemetry": {
    "recordTime": "2026-06-10 09:25:00",
    "temperature": 7.5,
    "humidity": 70.0,
    "doorOpen": false,
    "speed": 35.0,
    "outsideTemp": 31.0,
    "lng": 116.397,
    "lat": 39.908,
    "remainingKm": 13.4,
    "trend": "逼近上限"
  },
  "telemetryHistory": [
    { "recordTime": "2026-06-10 09:00:00", "temperature": 4.6 },
    { "recordTime": "2026-06-10 09:05:00", "temperature": 4.9 }
  ],
  "alerts": [
    {
      "alertId": "ALT-20260610-001",
      "level": "HIGH",
      "alertType": "TREND_WARNING",
      "triggerTime": "2026-06-10 09:25:00"
    }
  ]
}
```

**响应**

```json
{
  "anomalyDetected": true,
  "anomalyType": "TREND_RISE",
  "anomalyReason": "连续升温且已触发多条预警，存在温控失稳风险",
  "predictedMinutesToLimit": 12,
  "riskScore": 86.5,
  "riskLevel": "HIGH",
  "riskLabel": "高风险",
  "algorithmVersion": "risk-v2",
  "algorithmSource": "JAVA_HTTP",
  "recommendations": [
    {
      "planType": "REROUTE_COLD_STORAGE",
      "title": "改道最近冷库",
      "detail": "优先前往最近冷库进行临时控温，降低货损风险。",
      "estimatedCost": "增加 8 分钟路程成本",
      "estimatedBenefit": "预计 3 公里内恢复控温",
      "recommended": true
    },
    {
      "planType": "PRIORITY_DELIVERY",
      "title": "优先配送最近医院",
      "detail": "缩短高敏货物暴露时间，减少超温影响。",
      "estimatedCost": "需调整后续配送顺序",
      "estimatedBenefit": "可减少约 18 分钟暴露时间",
      "recommended": false
    }
  ]
}
```

### simulation/telemetry — 模拟遥测查询

**请求** `GET /simulation/telemetry`

```json
{
  "CC-VA-01": [
    {
      "recordTime": "2026-06-10 09:25:00",
      "temperature": 4.6,
      "humidity": 68.0,
      "doorOpen": false,
      "speed": 35.0,
      "outsideTemp": 31.0,
      "lng": 116.397,
      "lat": 39.908,
      "remainingKm": 13.4,
      "trend": "稳定"
    }
  ]
}
```

以 `vehicleCode` 为 key，每辆车返回一组遥测记录。

### health — 健康检查

`GET /health` — 用于 `AlgorithmGateway.status()` 判断服务可达性。

### 枚举约定

- 风险等级：`LOW` / `MEDIUM` / `HIGH`
- 时间格式：`yyyy-MM-dd HH:mm:ss`
- 布尔值：JSON `true`/`false`，禁止 `0`/`1`/`"yes"`/`"no"`

## 配置参考

```yaml
app:
  algorithm:
    mode: http-gateway              # mock | http | http-gateway
    version: mock-risk-v1
    fallback-enabled: true
    base-url: http://localhost:5001
    evaluate-path: /evaluate
    simulation-telemetry-path: /simulation/telemetry
```

| 配置项 | 说明 |
|---|---|
| `mode` | 网关模式 |
| `fallback-enabled` | HTTP 失败时是否降级到 MockEngine |
| `base-url` | 算法服务地址 |
| `evaluate-path` | 评估接口路径 |
| `simulation-telemetry-path` | 遥测数据拉取路径 |

## 数据同步（MySQL 模式）

以下接口仅在 `@Profile("mysql")` 下注册，需手动 POST 调用。

### 遥测导入

```
POST /api/v1/simulation/import-telemetry
```

1. 后端 GET 算法服务 `/simulation/telemetry`
2. 解析 JSON → `TelemetryRecordEntity` 列表
3. `saveAll()` → `telemetry_records` 表

### 分析导入

```
POST /api/v1/simulation/import-analysis
```

1. 遍历所有车辆，逐车 POST `/evaluate`
2. 结果写入：
   - `RiskAssessmentEntity` → `risk_assessments` 表
   - `RoutePlanEntity` → `route_plans` 表

每次调用会先清空相关表再全量写入。

### 推荐联调顺序

1. 确保算法服务启动 (`GET http://localhost:5001/health`)
2. `POST /api/v1/simulation/import-telemetry`
3. `POST /api/v1/simulation/import-analysis`
4. 前端刷新大屏查看结果

## 降级策略

- HTTP 调用超时或非 2xx 响应 → 抛出 `AlgorithmGatewayException`
- `fallback-enabled=true` → 自动切换到 `MockAlgorithmEngine` 返回兜底结果
- 前端通过 `GET /api/v1/algorithm/status` 可获知当前模式和来源

## 添加新算法能力

1. 在 `AlgorithmEvaluation` / `AlgorithmRecommendation` record 中加字段
2. 在 `HttpAlgorithmGateway.AlgorithmHttpResponse` 中加对应字段
3. 在 `MockAlgorithmEngine` 中提供 mock 默认值
4. 在 `AlgorithmAnalysisSyncService` 中处理新字段的落库
5. 如需新表，在 `schema.sql` 中建表并添加 JPA Entity + Repository
6. 更新接口契约（本文档和 `api-specification.md`）

## 关键 Java 类型

| 类 | 职责 |
|---|---|
| `AlgorithmGateway` | 算法调用统一接口 |
| `SwitchableAlgorithmGateway` | 根据配置切换 mock/HTTP，实现降级 |
| `MockAlgorithmEngine` | 纯 Java 规则引擎，开发期和降级兜底 |
| `HttpAlgorithmGateway` | HTTP 调用真实算法服务 |
| `AlgorithmEvaluation` | 评估结果 record（riskScore, riskLevel, recommendations...） |
| `AlgorithmRecommendation` | 单条路径建议 record（planType, title, detail...） |
| `AlgorithmGatewayStatus` | 网关状态 record（mode, available, version...） |
| `AlgorithmGatewayException` | 算法调用异常 |
| `AlgorithmSimulationSyncService` | 从算法服务拉遥测写入 telemetry_records |
| `AlgorithmAnalysisSyncService` | 调用 evaluate 并将结果写入 risk_assessments + route_plans |
