export const browserMockVehicles = [
  { vehicleId: "CC-VA-01", plateNumber: "京A-1024", cargoType: "VACCINE", cargoName: "疫苗", safeTempMin: 2, safeTempMax: 8, status: "IN_TRANSIT" },
  { vehicleId: "CC-VA-02", plateNumber: "京A-1025", cargoType: "VACCINE", cargoName: "疫苗", safeTempMin: 2, safeTempMax: 8, status: "IN_TRANSIT" },
  { vehicleId: "CC-VA-03", plateNumber: "京A-1026", cargoType: "VACCINE", cargoName: "疫苗", safeTempMin: 2, safeTempMax: 8, status: "IN_TRANSIT" },
  { vehicleId: "CC-VA-04", plateNumber: "京A-1027", cargoType: "VACCINE", cargoName: "疫苗", safeTempMin: 2, safeTempMax: 8, status: "IN_TRANSIT" },
  { vehicleId: "CC-VA-05", plateNumber: "京A-1028", cargoType: "VACCINE", cargoName: "疫苗", safeTempMin: 2, safeTempMax: 8, status: "IN_TRANSIT" },
];

const browserMockTelemetry = {
  "CC-VA-01": [
    { vehicleId: "CC-VA-01", recordTime: "2026-05-18 09:00:00", temperature: 4.6, humidity: 66, doorOpen: false, speed: 48, outsideTemp: 29, lng: 116.36, lat: 39.9, remainingKm: 26.8, trend: "温度平稳" },
    { vehicleId: "CC-VA-01", recordTime: "2026-05-18 09:05:00", temperature: 4.9, humidity: 65, doorOpen: false, speed: 46, outsideTemp: 29, lng: 116.372, lat: 39.904, remainingKm: 24.9, trend: "轻微升温" },
    { vehicleId: "CC-VA-01", recordTime: "2026-05-18 09:10:00", temperature: 5.4, humidity: 66, doorOpen: false, speed: 42, outsideTemp: 30, lng: 116.385, lat: 39.907, remainingKm: 22.2, trend: "连续升温" },
    { vehicleId: "CC-VA-01", recordTime: "2026-05-18 09:15:00", temperature: 6.1, humidity: 68, doorOpen: true, speed: 39, outsideTemp: 30, lng: 116.396, lat: 39.91, remainingKm: 20.5, trend: "开门导致升温" },
    { vehicleId: "CC-VA-01", recordTime: "2026-05-18 09:20:00", temperature: 6.8, humidity: 69, doorOpen: false, speed: 37, outsideTemp: 31, lng: 116.408, lat: 39.913, remainingKm: 16.8, trend: "12 分钟后可能越界" },
    { vehicleId: "CC-VA-01", recordTime: "2026-05-18 09:25:00", temperature: 7.5, humidity: 70, doorOpen: false, speed: 35, outsideTemp: 31, lng: 116.42, lat: 39.916, remainingKm: 13.4, trend: "逼近上限" },
  ],
  "CC-VA-02": [
    { vehicleId: "CC-VA-02", recordTime: "2026-05-18 09:25:00", temperature: 5.1, humidity: 64, doorOpen: false, speed: 43, outsideTemp: 30, lng: 116.402, lat: 39.91, remainingKm: 21.1, trend: "温度平稳" },
    { vehicleId: "CC-VA-02", recordTime: "2026-05-18 09:30:00", temperature: 5, humidity: 65, doorOpen: false, speed: 41, outsideTemp: 30, lng: 116.409, lat: 39.912, remainingKm: 19.4, trend: "温度平稳" },
  ],
  "CC-VA-03": [
    { vehicleId: "CC-VA-03", recordTime: "2026-05-18 09:25:00", temperature: 6.1, humidity: 67, doorOpen: true, speed: 18, outsideTemp: 30, lng: 116.41, lat: 39.906, remainingKm: 19.6, trend: "开门波动" },
    { vehicleId: "CC-VA-03", recordTime: "2026-05-18 09:30:00", temperature: 6.4, humidity: 68, doorOpen: false, speed: 24, outsideTemp: 30, lng: 116.416, lat: 39.909, remainingKm: 17.2, trend: "逐步回稳" },
  ],
  "CC-VA-04": [
    { vehicleId: "CC-VA-04", recordTime: "2026-05-18 09:25:00", temperature: 4.8, humidity: 62, doorOpen: false, speed: 46, outsideTemp: 29, lng: 116.421, lat: 39.912, remainingKm: 25.8, trend: "温度平稳" },
    { vehicleId: "CC-VA-04", recordTime: "2026-05-18 09:30:00", temperature: 4.9, humidity: 63, doorOpen: false, speed: 44, outsideTemp: 29, lng: 116.428, lat: 39.915, remainingKm: 24, trend: "温度平稳" },
  ],
  "CC-VA-05": [
    { vehicleId: "CC-VA-05", recordTime: "2026-05-18 09:25:00", temperature: 7.1, humidity: 69, doorOpen: false, speed: 30, outsideTemp: 32, lng: 116.43, lat: 39.918, remainingKm: 16.7, trend: "连续升温" },
    { vehicleId: "CC-VA-05", recordTime: "2026-05-18 09:30:00", temperature: 7.4, humidity: 70, doorOpen: false, speed: 28, outsideTemp: 32, lng: 116.436, lat: 39.921, remainingKm: 14.8, trend: "逼近上限" },
  ],
};

const browserMockAlerts = {
  "CC-VA-01": [
    [],
    [{ alertId: "ALT-20260518-000", vehicleId: "CC-VA-01", level: "LOW", title: "运输状态正常", detail: "温度处于安全温区内，当前状态稳定。", suggestion: "继续按照既定路线执行。", triggerTime: "2026-05-18 09:00:00" }],
    [{ alertId: "ALT-20260518-010", vehicleId: "CC-VA-01", level: "MEDIUM", title: "趋势预警触发", detail: "连续 3 个采样点出现升温趋势。", suggestion: "关注制冷设备负载变化。", triggerTime: "2026-05-18 09:10:00" }],
    [
      { alertId: "ALT-20260518-015", vehicleId: "CC-VA-01", level: "MEDIUM", title: "卸货开门温升", detail: "车门开启造成短时温升，风险增加。", suggestion: "完成作业后立即恢复制冷。", triggerTime: "2026-05-18 09:15:00" },
      { alertId: "ALT-20260518-010", vehicleId: "CC-VA-01", level: "MEDIUM", title: "趋势预警触发", detail: "连续 3 个采样点出现升温趋势。", suggestion: "关注制冷设备负载变化。", triggerTime: "2026-05-18 09:10:00" },
    ],
    [
      { alertId: "ALT-20260518-020", vehicleId: "CC-VA-01", level: "MEDIUM", title: "预测型预警", detail: "若当前趋势持续，未来 12 分钟可能突破 8°C。", suggestion: "优先配送最近的高敏货物。", triggerTime: "2026-05-18 09:20:00" },
      { alertId: "ALT-20260518-015", vehicleId: "CC-VA-01", level: "MEDIUM", title: "卸货开门温升", detail: "车门开启造成短时温升，风险增加。", suggestion: "完成作业后立即恢复制冷。", triggerTime: "2026-05-18 09:15:00" },
      { alertId: "ALT-20260518-010", vehicleId: "CC-VA-01", level: "MEDIUM", title: "趋势预警触发", detail: "连续 3 个采样点出现升温趋势。", suggestion: "关注制冷设备负载变化。", triggerTime: "2026-05-18 09:10:00" },
    ],
    [
      { alertId: "ALT-20260518-025", vehicleId: "CC-VA-01", level: "HIGH", title: "高风险临界告警", detail: "疫苗车厢温度接近安全上限，剩余路线仍较长。", suggestion: "比较冷库改道方案与继续配送方案的综合成本。", triggerTime: "2026-05-18 09:25:00" },
      { alertId: "ALT-20260518-020", vehicleId: "CC-VA-01", level: "MEDIUM", title: "预测型预警", detail: "若当前趋势持续，未来 12 分钟可能突破 8°C。", suggestion: "优先配送最近的高敏货物。", triggerTime: "2026-05-18 09:20:00" },
      { alertId: "ALT-20260518-015", vehicleId: "CC-VA-01", level: "MEDIUM", title: "卸货开门温升", detail: "车门开启造成短时温升，风险增加。", suggestion: "完成作业后立即恢复制冷。", triggerTime: "2026-05-18 09:15:00" },
      { alertId: "ALT-20260518-010", vehicleId: "CC-VA-01", level: "MEDIUM", title: "趋势预警触发", detail: "连续 3 个采样点出现升温趋势。", suggestion: "关注制冷设备负载变化。", triggerTime: "2026-05-18 09:10:00" },
    ],
  ],
  "CC-VA-03": [
    [{ alertId: "ALT-20260518-301", vehicleId: "CC-VA-03", level: "MEDIUM", title: "卸货开门温升", detail: "车门开启造成温度快速上升。", suggestion: "缩短开门时长，及时恢复制冷。", triggerTime: "2026-05-18 09:25:00" }],
  ],
  "CC-VA-05": [
    [{ alertId: "ALT-20260518-501", vehicleId: "CC-VA-05", level: "MEDIUM", title: "温升关注", detail: "温度持续爬升，接近高风险区间。", suggestion: "优先保障制冷能力，减少停留。", triggerTime: "2026-05-18 09:25:00" }],
    [
      { alertId: "ALT-20260518-502", vehicleId: "CC-VA-05", level: "HIGH", title: "逼近上限预警", detail: "当前温度接近安全上限。", suggestion: "建议尽快完成近端配送或切换备选路线。", triggerTime: "2026-05-18 09:30:00" },
      { alertId: "ALT-20260518-501", vehicleId: "CC-VA-05", level: "MEDIUM", title: "温升关注", detail: "温度持续爬升，接近高风险区间。", suggestion: "优先保障制冷能力，减少停留。", triggerTime: "2026-05-18 09:25:00" },
    ],
  ],
};

const browserMockCursors = new Map();

export async function createBrowserMockDashboard(vehicleId, alertLimit) {
  const telemetryScenario = browserMockTelemetry[vehicleId] || browserMockTelemetry["CC-VA-01"];
  const cursor = browserMockCursors.get(vehicleId) || 0;
  const telemetry = telemetryScenario[cursor % telemetryScenario.length];
  const alertScenario = browserMockAlerts[vehicleId] || [[]];
  const alerts = alertScenario[Math.min(cursor, alertScenario.length - 1)] || [];

  browserMockCursors.set(vehicleId, cursor + 1);

  return [
    { data: telemetry, source: "browser-mock" },
    { data: alerts.slice(0, alertLimit), source: "browser-mock" },
  ];
}
