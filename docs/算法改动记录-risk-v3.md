# 算法改动记录 — risk-v3

**日期**：2026-06-11  
**改动项**：趋势型异常防误报机制

---

## 改了什么

`AnomalyDetector.java` — 新增防误报机制。

## 为什么改

之前只要一次最小二乘斜率超标就立刻报 `TREND_RISE`。温度传感器有随机噪声，比如偶尔一个 0.2°C 的抖动就可能导致误报。这对演示体验和算法可信度都不好。

## 怎么改的

加入**连续确认计数器**：

- 趋势型异常（`TREND_RISE`）首次触发时**不立即报告**，只记录"趋势预警收集中"
- 连续触发 TREND_CONFIRM_REQUIRED（默认 2）次后，正式确认异常
- 确认后只要趋势还在就持续报告，不需要重新确认
- 温度恢复正常（趋势消失）时，计数器自动清零

**硬事件不受影响**：阈值越界（`THRESHOLD_BREACH`）和车门事件（`DOOR_EVENT`）照常立即报告。

## 改动范围

| 文件 | 行数变化 | 说明 |
|------|---------|------|
| `AnomalyDetector.java` | +30 行 | 新增 `trendConfirmCounters` (ConcurrentHashMap) + `applyTrendAntiFlapping()` 方法 |

零影响其他模块。

## 可调参数

| 常量 | 默认值 | 含义 |
|------|-------|------|
| `TREND_CONFIRM_REQUIRED` | 2 | 连续触发次数阈值。调大 = 更保守、更慢响应；调小 = 更敏感 |

在 `AnomalyDetector.java` 第 33 行修改即可。
