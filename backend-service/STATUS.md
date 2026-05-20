# 项目状态

## 当前状态

- 已迁移为正式 Spring Boot 工程
- 已按 PRD 建立标准后端分层
- 已完成第 1 周目标，并补充了第 2 周联调用的部分查询接口

## 已实现接口

- `GET /health`
- `GET /api/v1/vehicles`
- `GET /api/v1/dashboard/vehicles/{vehicleCode}`
- `GET /api/v1/vehicles/{vehicleCode}/telemetry/latest`
- `GET /api/v1/vehicles/{vehicleCode}/telemetry/history?minutes=30`
- `GET /api/v1/vehicles/{vehicleCode}/alerts?limit=4`
- `GET /api/v1/alerts?vehicleCode=CC-VA-01&page=1&pageSize=20`
- `GET /api/v1/alerts/{alertId}`

## 当前数据状态

- 主场景：疫苗冷链配送
- 模拟车辆：5 辆
- 单车演示链路：CC-VA-01
- 模拟数据：温度、湿度、速度、车门状态、经纬度、剩余里程、告警

## 待推进

- MySQL 接入
- 风险评分接口
- 算法服务联调
- 路径建议动态计算
