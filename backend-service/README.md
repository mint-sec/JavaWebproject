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
- 统一响应结构
- 全局异常处理
- `X-Trace-Id` 请求跟踪
- 首页与 `/api-docs` 接口文档页
- 第 1 周基础接口
- 大屏聚合接口
- 历史温度查询接口
- 告警列表与告警详情接口

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

## 关键地址

- `/`
- `/health`
- `/api-docs`
- `/api/v1/vehicles`
- `/api/v1/dashboard/vehicles/CC-VA-01`

## 下一步建议

- 接入 MySQL 持久化
- 把 mock 数据替换成 repository 层真实数据
- 增加风险评估接口
- 增加算法服务调用封装
