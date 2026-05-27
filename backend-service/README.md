# backend-service

按 PRD 正式推进的 Java Spring Boot 后端项目。

## 当前定位

- 技术栈：Java 17 + Spring Boot
- 当前阶段：完成第 1 周骨架，并补充了 PRD 中前后端联调所需的核心查询接口
- 数据来源：内存模拟数据
- 前端影响：不修改任何 `frontend/` 文件

## 目录结构

```text
backend-service/
  pom.xml
  src/
    main/
      java/
      resources/
  sql/
  scripts/
  API.md
  README.md
  STATUS.md
```

## 已实现内容

- Spring Boot 工程骨架
- `controller / service / repository / dto / entity / config / exception` 分层
- JPA / MySQL 持久化骨架
- 统一响应结构
- 全局异常处理
- `X-Trace-Id` 请求跟踪
- 首页与 `/api-docs` 接口文档页
- 第 1 周基础接口
- 大屏聚合接口
- 历史温度查询接口
- 告警列表与告警详情接口
- 告警汇总接口
- 风险评估接口与风险历史接口
- 路径建议接口
- mock / HTTP 可切换算法网关与降级结构
- 动态 mock 时间轴，支持轮询时数据推进

## 启动方式

```powershell
powershell -ExecutionPolicy Bypass -File .\backend-service\scripts\start.ps1
```

指定端口启动：

```powershell
powershell -ExecutionPolicy Bypass -File .\backend-service\scripts\start.ps1 -Port 18081
```

说明：

- 启动脚本会优先使用 `mvnw.cmd`
- 如果本机 PATH 中存在 `mvn`，也会直接使用
- 如果都没有，会尝试查找 JetBrains 自带 Maven
- 当前默认数据源模式是 `mock`
- 当前默认关闭 JPA / DataSource 自动装配，保证 mock 模式可直接启动
- 如需切 MySQL，请先准备数据库，再恢复 DataSource/JPA 自动装配并把 `app.datasource.mode` 改成 `mysql`

## 关键地址

- `/`
- `/health`
- `/api-docs`
- `/api/v1/vehicles`
- `/api/v1/dashboard/vehicles/CC-VA-01`
- `/api/v1/vehicles/CC-VA-01/risk-assessments/latest`
- `/api/v1/vehicles/CC-VA-01/route-plans/latest`
- `/api/v1/algorithm/status`

## 下一步建议

- 完成 MySQL 查询实现并替换当前 mock repository 读取链路
- 把 mock 算法网关替换成真实 Python 算法服务
- 增加告警确认/关闭等写接口
