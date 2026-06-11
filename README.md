# 冷链运输温控预警平台

对冷链运输过程中的温度、位置、车门状态等信息进行实时监控，在异常趋势出现时提前预警，并结合货物风险与路线约束给出调度建议。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端服务 | Java 17, Spring Boot 3, Spring Data JPA, Maven |
| 算法服务 | Java 17, Spring Boot 3 (独立进程) |
| 前端 | Vue 3 (Composition API), Vite, Axios |
| 数据库 | MySQL 8.x |
| 认证 | JWT (Bearer Token) |

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                      浏览器 (Vue 3)                      │
│              前端工作台 / 管理后台 / 大屏                  │
└──────────────┬──────────────────────────────────────────┘
               │ HTTP (Bearer JWT)
               ▼
┌──────────────────────────────┐     ┌────────────────────┐
│       backend-service        │────▶│ algorithm-service  │
│       Spring Boot :18081     │HTTP │ Spring Boot :5001  │
│  ┌────────────────────────┐  │     │  异常检测/风险评估   │
│  │  AuthFilter (JWT校验)   │  │     │  路径规划/模拟遥测   │
│  │  Controller → Service   │  │     └────────────────────┘
│  │  → Repository (双模式)  │  │
│  │   MockDataRepo / JPA    │  │
│  └────────────────────────┘  │
└──────────────┬───────────────┘
               │
               ▼
       ┌──────────────┐
       │   MySQL 8.x   │
       │ coldchain_mvp │
       └──────────────┘
```

核心设计：
- **双数据源模式**：`app.datasource.mode=mock|mysql` 控制走内存模拟数据还是 MySQL JPA
- **可切换算法网关**：`app.algorithm.mode=mock|http` 控制走内置规则引擎还是远程 Python 算法服务，HTTP 失败可自动降级
- **AuthFilter**：拦截所有 `/api/v1/*` 请求，仅放行 login/register，其余校验 JWT + 角色鉴权

## 快速启动

### 前置要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.x (mock 模式可跳过)

### 1. 启动算法服务

```bash
cd algorithm-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=5001"
```

默认端口 `5001`。`GET /health` 验证启动。

### 2. 启动后端服务

```bash
cd backend-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=18081"
```

默认以 mock 数据源模式启动。如需 MySQL 模式：

```bash
# 先执行 sql/schema.sql 和 sql/seed.sql 建库
# 复制 application-local.example.yml → application-local.yml 并配置数据库连接
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql --server.port=18081"
```

验证地址：
- 首页：`http://localhost:18081/`
- 健康检查：`http://localhost:18081/health`
- 接口文档：`http://localhost:18081/api-docs`
- 车辆列表：`http://localhost:18081/api/v1/vehicles`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认端口 `18080`，代理后端 `18081`。

### 推荐启动顺序

```
MySQL (如需) → algorithm-service → backend-service → frontend
```

## 目录结构

```
├── algorithm-service/          # 算法数据服务 (Spring Boot)
│   └── src/main/java/com/coldchain/algorithm/
│       ├── controller/         # EvaluateController, SimulationController
│       ├── service/            # AnomalyDetector, RiskScorer, RoutePlanner
│       ├── model/              # EvaluateRequest, EvaluateResponse
│       └── data/               # DataGenerator
│
├── backend-service/            # 后端 API 服务 (Spring Boot)
│   └── src/main/java/com/coldchain/backend/
│       ├── config/             # AuthFilter, JwtUtil, WebConfig
│       ├── controller/         # 8 个 Controller (Auth, Vehicle, Dashboard, ...)
│       ├── service/            # 15+ Service + algorithm/ 子包
│       ├── repository/         # MockDataRepository, JPA Repositories
│       ├── entity/             # JPA Entity + Java Record
│       ├── dto/                # 26 个请求/响应 DTO
│       └── exception/          # GlobalExceptionHandler
│
├── frontend/                   # 前端 (Vue 3 + Vite)
│   └── src/
│       ├── components/         # 10 个 Vue 组件
│       ├── composables/        # useDashboard, dashboardMock, dashboardUtils
│       ├── services/           # apiClient, authService, adminService, ...
│       ├── App.vue
│       └── main.js
│
└── docs/                       # 项目文档
    ├── architecture.md         # 架构设计
    ├── api-specification.md    # 接口规范与字段字典
    ├── database.md             # 数据库设计
    └── algorithm-integration.md # 算法对接说明
```

## 功能模块

| 模块 | 功能 |
|---|---|
| 认证授权 | JWT 登录/注册/登出，基于角色的路由鉴权 (ADMIN/USER) |
| 监控大屏 | 车辆实时温湿度、位置、速度、风险等级、温度趋势图、告警面板 |
| 温控预警 | 阈值越界告警、趋势预警、告警分级与处置建议 |
| 风险评估 | 风险评分、风险等级、预计超限时间 |
| 路径建议 | 继续配送 / 优先配送最近站点 / 改道最近冷库 |
| 历史查询 | 温度历史、告警记录、风险评估记录、路径建议记录 |
| 管理后台 | 平台概览、用户管理 (角色/封禁)、登录/操作日志、服务监控 |
| 业务工作区 | 我的车辆 CRUD、我的告警处理 |

## 默认演示账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | Admin123! | ADMIN |
| operator | User1234! | USER |

主演示车辆：`CC-VA-01`（疫苗冷链配送，数据最完整）

## 环境配置

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `server.port` (backend) | 18081 | 后端服务端口 |
| `server.port` (algorithm) | 5001 | 算法服务端口 |
| `app.datasource.mode` | mock | mock \| mysql |
| `app.algorithm.mode` | mock | mock \| http \| http-gateway |
| `app.algorithm.fallback-enabled` | true | HTTP 失败时降级到 mock |
| `app.algorithm.base-url` | http://localhost:5001 | 算法服务地址 |

## 相关文档

- [架构设计](docs/architecture.md)
- [接口规范](docs/api-specification.md)
- [数据库设计](docs/database.md)
- [算法对接](docs/algorithm-integration.md)
