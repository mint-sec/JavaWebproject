# 项目状态

## 当前阶段

- 当前状态：已完成第 1 周后端任务
- 主场景：疫苗冷链配送
- 模拟车辆：5 辆
- 运行方式：Java 17 本地编译启动

## 已完成内容

- 独立后端目录已建立
- 初版数据库表结构已输出到 [schema.sql](/E:/codex_project/JavaWebproject/backend-service/sql/schema.sql)
- 初版种子数据已输出到 [seed.sql](/E:/codex_project/JavaWebproject/backend-service/sql/seed.sql)
- 3 个基础接口已实现
- 模拟数据读取逻辑已实现

## 接口清单

### 1. 获取车辆信息

- Method: `GET`
- Path: `/api/v1/vehicles`

### 2. 获取车辆最新温度数据

- Method: `GET`
- Path: `/api/v1/vehicles/{vehicleId}/telemetry/latest`

### 3. 获取车辆告警记录

- Method: `GET`
- Path: `/api/v1/vehicles/{vehicleId}/alerts`
- Query: `limit` 可选

## 下一步

- 补历史温度查询接口
- 补历史告警查询接口
- 预留算法服务调用入口
- 把返回结构和错误处理继续标准化
