# 后端接口文档

适用项目：`backend-service`

## 1. 服务说明

- 服务名称：冷链运输温控预警平台后端服务
- 默认端口：`18081`
- 根路径：`http://localhost:18081/`
- 健康检查：`GET /health`
- 接口前缀：`/api/v1`

## 2. 通用返回结构

所有接口统一返回：

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

字段说明：

- `success`：请求是否成功
- `message`：结果说明
- `data`：业务数据

## 3. 接口清单

### 3.1 获取车辆列表

- Method: `GET`
- Path: `/api/v1/vehicles`

响应示例：

```json
{
  "success": true,
  "message": "ok",
  "data": [
    {
      "vehicleId": "CC-VA-01",
      "plateNumber": "京A-1024",
      "cargoType": "VACCINE",
      "cargoName": "疫苗",
      "safeTempMin": 2.0,
      "safeTempMax": 8.0,
      "status": "IN_TRANSIT"
    }
  ]
}
```

### 3.2 获取车辆最新温度数据

- Method: `GET`
- Path: `/api/v1/vehicles/{vehicleId}/telemetry/latest`

示例：

- `/api/v1/vehicles/CC-VA-01/telemetry/latest`

响应示例：

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "vehicleId": "CC-VA-01",
    "recordTime": "2026-05-18 09:25:00",
    "temperature": 7.5,
    "humidity": 70,
    "doorOpen": false,
    "speed": 35,
    "outsideTemp": 31,
    "lng": 116.4,
    "lat": 39.91,
    "remainingKm": 13.4,
    "trend": "逼近上限"
  }
}
```

### 3.3 获取车辆告警记录

- Method: `GET`
- Path: `/api/v1/vehicles/{vehicleId}/alerts`
- Query: `limit` 可选

示例：

- `/api/v1/vehicles/CC-VA-01/alerts`
- `/api/v1/vehicles/CC-VA-01/alerts?limit=1`

响应示例：

```json
{
  "success": true,
  "message": "ok",
  "data": [
    {
      "alertId": "ALT-20260518-001",
      "vehicleId": "CC-VA-01",
      "level": "HIGH",
      "title": "高风险临界告警",
      "detail": "疫苗车厢温度接近安全上限，剩余路线较长。",
      "suggestion": "比较最近冷库改道方案与继续配送方案的综合成本。",
      "triggerTime": "2026-05-18 09:25:00"
    }
  ]
}
```

## 4. 可直接访问的地址

- `http://localhost:18081/`
- `http://localhost:18081/api-docs`
- `http://localhost:18081/health`
- `http://localhost:18081/api/v1/vehicles`

## 5. 当前范围说明

当前是第 1 周后端版本，只覆盖：

- 车辆基础信息
- 最新温度数据
- 告警记录查询

下一步将补充：

- 历史温度接口
- 历史告警接口
- 风险评估接口
- 前端聚合接口
