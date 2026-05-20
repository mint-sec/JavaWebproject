# 后端接口文档

适用项目：`backend-service`

## 服务信息

- 技术栈：Spring Boot
- 默认端口：`18081`
- 接口前缀：`/api/v1`
- 文档页：`/api-docs`

## 通用返回结构

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

## 1. 健康检查

- Method: `GET`
- Path: `/health`

## 2. 获取车辆列表

- Method: `GET`
- Path: `/api/v1/vehicles`

## 3. 获取大屏聚合数据

- Method: `GET`
- Path: `/api/v1/dashboard/vehicles/{vehicleCode}`

用途：

- 给前端大屏一次性返回车辆、温度趋势、告警和路线展示数据

## 4. 获取车辆最新温度数据

- Method: `GET`
- Path: `/api/v1/vehicles/{vehicleCode}/telemetry/latest`

## 5. 获取历史温度数据

- Method: `GET`
- Path: `/api/v1/vehicles/{vehicleCode}/telemetry/history`
- Query: `minutes`

## 6. 获取车辆告警记录

- Method: `GET`
- Path: `/api/v1/vehicles/{vehicleCode}/alerts`
- Query: `limit`

## 7. 获取分页告警列表

- Method: `GET`
- Path: `/api/v1/alerts`
- Query:
  - `vehicleCode`
  - `page`
  - `pageSize`

## 8. 获取告警详情

- Method: `GET`
- Path: `/api/v1/alerts/{alertId}`

## 说明

当前版本仍使用 mock 数据，目的是先满足 PRD 中：

- 后端服务成型
- 数据结构稳定
- 前端接口可联调
- 后续算法接入点清晰
