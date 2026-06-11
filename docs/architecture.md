# 架构设计

## 系统架构

```
┌──────────────────────────────────────────────────────────────────┐
│                          浏览器 (Vue 3)                          │
│       AuthPortal / DashboardView / AdminConsole / Workspace      │
└──────────────┬───────────────────────────────────────────────────┘
               │ HTTP REST + Bearer JWT
               ▼
┌──────────────────────────────────────────────────────────────────┐
│                      backend-service (18081)                      │
│                                                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ AuthFilter│──▶│JwtUtil   │──▶│Controller│──▶│Service   │        │
│  │ (Bearer)  │  │(解析/校验)│  │(路由分发) │  │(业务编排) │        │
│  └──────────┘  └──────────┘  └──────────┘  └────┬─────┘        │
│                                                  │                │
│                    ┌─────────────────────────────┤                │
│                    │                             │                │
│              ┌─────▼──────┐          ┌───────────▼───────────┐   │
│              │ MockDataRepo│          │ JPA Repositories     │   │
│              │ (内存模拟)   │          │ (MySQL 持久化)       │   │
│              └────────────┘          └───────────────────────┘   │
│                    │                             │                │
│              DataSourceModeProperties 控制切换                    │
└────────────────────┼─────────────────────────────────────────────┘
                     │ HTTP (evaluate / telemetry)
                     ▼
┌──────────────────────────────────────────────────────────────────┐
│                      algorithm-service (5001)                     │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────────┐     │
│  │AnomalyDetector│  │  RiskScorer   │  │   RoutePlanner   │     │
│  │  异常检测      │  │  风险评分      │  │   路径重规划      │     │
│  └───────────────┘  └───────────────┘  └──────────────────┘     │
└──────────────────────────────────────────────────────────────────┘
```

## 模块职责

### backend-service（核心业务服务）

| 层 | 职责 |
|---|---|
| `config/` | AuthFilter(JWT校验+角色鉴权)、JwtUtil、TraceIdFilter、WebConfig(CORS)、DataSourceModeProperties |
| `controller/` | 8 个 Controller，统一返回 `ApiResponse<T>` |
| `service/` | 15 个业务 Service + `algorithm/` 算法网关子包 |
| `repository/` | `MockDataRepository`(内存)、`AdminDataRepository`(内存)、`AuditLogRepository`(内存)、`mysql/*JpaRepository`(JPA) |
| `entity/` | Java Record (内存模式) + JPA Entity (MySQL 模式) |
| `dto/` | 请求/响应 DTO，字段与前端强关联 |
| `exception/` | GlobalExceptionHandler (统一异常处理)、AuthException、NotFoundException |

### algorithm-service（算法数据服务）

| 组件 | 职责 |
|---|---|
| `AnomalyDetector` | 异常检测 (阈值 + 趋势) |
| `RiskScorer` | 风险评分与等级映射 |
| `RoutePlanner` | 路径重规划建议生成 |
| `DataGenerator` | 模拟遥测数据生成 |
| `RealtimeSimulationService` | 实时仿真数据推送 |

### frontend（前端）

基于 Vue 3 Composition API，使用 `App.vue` 根据 `currentUser` 状态切换视图。核心数据流通过 `useDashboard` composable 管理，`apiClient.js` (axios) 统一处理请求拦截和 token 注入。

## 认证流程

```
请求到达
    │
    ▼
┌─────────────┐     否     ┌──────────┐
│ 路径以       │──────────▶│  放行     │
│ /api/v1 开头? │           └──────────┘
└──────┬──────┘
       │ 是
       ▼
┌─────────────┐     是     ┌──────────┐
│ 在白名单     │──────────▶│  放行     │
│ (login,      │           └──────────┘
│  register)?  │
└──────┬──────┘
       │ 否
       ▼
┌─────────────┐     无效    ┌──────────┐
│ 提取并校验   │──────────▶│ 401 未登录│
│ Bearer Token│           └──────────┘
└──────┬──────┘
       │ 有效
       ▼
┌─────────────┐     用户不存在/已封禁
│ JWT 解析     │──────────▶ 401/403
│ 查用户信息    │
└──────┬──────┘
       │ 通过
       ▼
┌─────────────┐    admin路由   ┌──────────┐
│ 角色校验     │ + 非ADMIN角色  │  403     │
└──────┬──────┘──────────────▶ └──────────┘
       │ 通过
       ▼
┌─────────────┐
│ 放行, 注入   │
│ userId+role │
└─────────────┘
```

认证白名单：`/api/v1/auth/login`、`/api/v1/auth/register`

JWT 签发时写入 `userId`、`role`、`username`，AuthFilter 解析后注入请求属性供 Controller 使用。

## 数据源双模式

通过 `app.datasource.mode` 配置项控制：

| 模式 | 行为 | 适用场景 |
|---|---|---|
| `mock` | 走 `MockDataRepository`、`AdminDataRepository` 等内存实现，预置 5 辆车的完整演示数据 | 开发期、演示、无 MySQL 环境 |
| `mysql` | 走 `*JpaRepository`，Spring Data JPA 操作 MySQL | 联调、生产 |

切换方式：修改 `application.yml` 中 `app.datasource.mode` 或在 IDEA 启动参数加 `--app.datasource.mode=mysql`。

## 算法网关策略模式

```
                    ┌─────────────────────┐
                    │  AlgorithmGateway   │  (接口)
                    │  evaluate()         │
                    │  status()           │
                    └──────────┬──────────┘
                               │
              ┌────────────────┴────────────────┐
              ▼                                 ▼
┌──────────────────────────┐     ┌──────────────────────────┐
│ SwitchableAlgorithmGateway│     │   MockAlgorithmEngine    │
│ (根据配置切换)             │     │   (规则引擎)              │
│                          │     │   温度≥6.5 或告警≥2→异常   │
│  mode=mock → MockEngine  │     │   分数≥80→HIGH            │
│  mode=http → HTTP调用     │     │   分数≥45→MEDIUM          │
│  HTTP失败+fallback→降级   │     │   否则→LOW               │
└───────────┬──────────────┘     └──────────────────────────┘
            │
            ▼
┌──────────────────────────┐
│  HttpAlgorithmGateway    │
│  POST {base-url}/evaluate│
│  GET  {base-url}/health  │
└──────────────────────────┘
```

降级策略：HTTP 调用超时或非 2xx 响应时，若 `fallback-enabled=true`，自动切换到 `MockAlgorithmEngine`，前端不受影响。

## 数据同步流程（MySQL 模式）

```
POST /api/v1/simulation/import-telemetry
    │
    ▼
后端 ──GET──▶ algorithm-service /simulation/telemetry
    ◀── JSON { CC-VA-01: [telemetry...] }
    │
    ▼
TelemetryRecordEntity → saveAll() → telemetry_records 表

POST /api/v1/simulation/import-analysis
    │
    ▼
for each vehicle:
    后端 ──POST──▶ algorithm-service /evaluate
        ◀── AlgorithmEvaluation
        │
        ├── risk_assessments 表 (RiskAssessmentEntity)
        └── route_plans 表 (RoutePlanEntity)
```

两个同步接口仅在使用 `@Profile("mysql")` 时注册，需手动 POST 调用。

## TraceId 贯穿

`TraceIdFilter` 为每个请求生成唯一 `X-Trace-Id`，写入 MDC 和响应头，异常响应也包含 traceId，方便日志追踪和问题排查。

## 跨域策略

`WebConfig` 配置 CORS，允许 `localhost:18080` 前端开发服务器跨域访问，允许所有 HTTP 方法和常用请求头。

## 关键设计决策

1. **统一返回格式**：所有接口返回 `{ success, message, data }`，异常也走此结构，前端无需区分处理
2. **字段统一小驼峰**：JSON 字段全部 camelCase，前端、后端、算法三方协议一致
3. **vehicleCode 作为业务主键**：前后端统一使用 `vehicleCode`（如 `CC-VA-01`），不使用数据库自增 ID
4. **枚举值后端英文/前端中文**：接口传英文枚举（`HIGH`/`MEDIUM`/`LOW`），前端自行做中文映射，`roleLabel`/`levelLabel` 等标签字段后端直接返回中文
5. **管理员与业务分离**：管理后台 (`/admin/*`) 只做平台管理，业务工作区 (`/workspace/*`) 做车辆和告警的日常操作
