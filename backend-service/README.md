# backend-service

独立后端项目目录，只承接当前仓库里的后端开发工作，不改动现有前端代码。

## 当前实现

- Java 17 可直接编译运行
- 3 个基础接口已完成
- 5 辆疫苗冷链车模拟数据已内置
- MySQL 初版表结构和种子数据已提供

## 目录结构

```text
backend-service/
  src/
  sql/
  scripts/
  API.md
  README.md
  STATUS.md
```

## 默认端口

- 默认端口：`18081`
- 可通过环境变量 `COLDCHAIN_PORT` 或启动脚本参数覆盖

## 启动方式

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\backend-service\scripts\start.ps1
```

指定端口启动：

```powershell
powershell -ExecutionPolicy Bypass -File .\backend-service\scripts\start.ps1 -Port 18081
```

## 接口

- `GET /api/v1/vehicles`
- `GET /api/v1/vehicles/{vehicleId}/telemetry/latest`
- `GET /api/v1/vehicles/{vehicleId}/alerts`

示例：

- `http://localhost:18081/api/v1/vehicles`
- `http://localhost:18081/api/v1/vehicles/CC-VA-01/telemetry/latest`
- `http://localhost:18081/api/v1/vehicles/CC-VA-01/alerts?limit=1`
- `http://localhost:18081/api-docs`

## 后续建议

- 下一步优先补历史查询接口
- 再接入算法服务做异常检测和风险评分
- 最后补统一返回 DTO、日志和配置文件
