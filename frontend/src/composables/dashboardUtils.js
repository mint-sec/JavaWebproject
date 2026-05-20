export const POLL_INTERVAL_MS = 5000;
export const MAX_HISTORY = 12;
export const MAX_ALERTS = 4;
export const levelPriority = { LOW: 0, MEDIUM: 1, HIGH: 2 };

export function normalizeLevel(level) {
  return String(level || "LOW").trim().toUpperCase();
}

export function toneClass(level) {
  const normalized = normalizeLevel(level);
  if (normalized === "HIGH") {
    return "danger";
  }
  if (normalized === "MEDIUM") {
    return "warn";
  }
  return "";
}

export function levelLabel(level) {
  const normalized = normalizeLevel(level);
  if (normalized === "HIGH") {
    return "高风险";
  }
  if (normalized === "MEDIUM") {
    return "中风险";
  }
  return "低风险";
}

export function vehicleStatusLabel(status) {
  if (status === "IN_TRANSIT") {
    return "运输中";
  }
  if (status === "IDLE") {
    return "待命中";
  }
  if (status === "OFFLINE") {
    return "离线";
  }
  return status || "未知";
}

export function formatClock(date) {
  const value = typeof date === "string" ? new Date(date) : date;
  if (!(value instanceof Date) || Number.isNaN(value.getTime())) {
    return "--:--:--";
  }
  return value.toLocaleTimeString("zh-CN", { hour12: false });
}

export function formatNumber(value, digits = 1) {
  return Number(value).toFixed(digits).replace(/\.0$/, "");
}

export function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max);
}

export function deriveRiskLevel(vehicle, telemetry) {
  if (!vehicle || !telemetry) {
    return "LOW";
  }
  if (telemetry.temperature >= vehicle.safeTempMax || telemetry.temperature <= vehicle.safeTempMin) {
    return "HIGH";
  }
  if (telemetry.temperature >= vehicle.safeTempMax - 0.6 || telemetry.temperature <= vehicle.safeTempMin + 0.6 || telemetry.doorOpen) {
    return "MEDIUM";
  }
  return "LOW";
}

export function getHighestAlertLevel(alerts) {
  if (!alerts.length) {
    return "";
  }
  return alerts.reduce((highest, current) => {
    const normalized = normalizeLevel(current.level);
    return levelPriority[normalized] > levelPriority[highest] ? normalized : highest;
  }, "LOW");
}

export function normalizeVehicle(item) {
  return {
    ...item,
    vehicleId: item.vehicleId || item.vehicleCode || "",
  };
}

export function normalizeTelemetry(item) {
  return {
    vehicleId: item.vehicleId || item.vehicleCode || "",
    recordTime: item.recordTime,
    temperature: Number(item.temperature) || 0,
    humidity: Number(item.humidity) || 0,
    doorOpen: Boolean(item.doorOpen),
    speed: Number(item.speed) || 0,
    outsideTemp: Number(item.outsideTemp) || 0,
    lng: Number(item.lng) || 0,
    lat: Number(item.lat) || 0,
    remainingKm: Number(item.remainingKm) || 0,
    trend: item.trend || "温度平稳",
  };
}

export function normalizeHistoryPoint(item, fallback = {}) {
  return {
    sampleTime: item.sampleTime || item.time || item.recordTime?.split(" ")[1] || "--:--",
    recordTime: item.recordTime || fallback.recordTime || "",
    temperature: Number(item.temperature ?? fallback.temperature) || 0,
    humidity: Number(item.humidity ?? fallback.humidity) || 0,
    doorOpen: Boolean(item.doorOpen ?? fallback.doorOpen),
    speed: Number(item.speed ?? fallback.speed) || 0,
    outsideTemp: Number(item.outsideTemp ?? fallback.outsideTemp) || 0,
    lng: Number(item.lng ?? fallback.lng) || 0,
    lat: Number(item.lat ?? fallback.lat) || 0,
    remainingKm: Number(item.remainingKm ?? fallback.remainingKm) || 0,
    trend: item.trend || fallback.trend || "娓╁害骞崇ǔ",
  };
}

export function buildDashboardHistory(items, latestTelemetry, route) {
  const historyItems = Array.isArray(items) ? items : [];
  const routePoints = [
    ...(Array.isArray(route?.pathPoints) ? route.pathPoints : []),
    ...(route?.currentPosition ? [route.currentPosition] : []),
  ];
  const fallbackPoint = routePoints[routePoints.length - 1] || latestTelemetry || {};

  if (!historyItems.length) {
    return latestTelemetry ? [normalizeHistoryPoint(latestTelemetry, fallbackPoint)] : [];
  }

  return historyItems.map((item, index) => {
    const routeIndex =
      routePoints.length > 1
        ? Math.round((index / Math.max(historyItems.length - 1, 1)) * (routePoints.length - 1))
        : 0;
    const routePoint = routePoints[routeIndex] || fallbackPoint;
    return normalizeHistoryPoint(item, {
      ...latestTelemetry,
      ...routePoint,
    });
  });
}

export function normalizeAlerts(items) {
  return (Array.isArray(items) ? items : [])
    .map((item) => ({
      ...item,
      vehicleId: item.vehicleId || item.vehicleCode || "",
      level: normalizeLevel(item.level),
    }))
    .sort((left, right) => levelPriority[normalizeLevel(right.level)] - levelPriority[normalizeLevel(left.level)]);
}

export function mapTrajectoryToCanvas(points) {
  const width = 720;
  const height = 380;
  const padding = 60;
  const lngValues = points.map((point) => point.lng);
  const latValues = points.map((point) => point.lat);
  const minLng = Math.min(...lngValues);
  const maxLng = Math.max(...lngValues);
  const minLat = Math.min(...latValues);
  const maxLat = Math.max(...latValues);
  const lngRange = maxLng - minLng || 0.02;
  const latRange = maxLat - minLat || 0.02;

  return points.map((point) => ({
    ...point,
    x: padding + ((point.lng - minLng) / lngRange) * (width - padding * 2),
    y: height - padding - ((point.lat - minLat) / latRange) * (height - padding * 2),
  }));
}

export function buildApiBaseCandidates(apiBaseFromQuery) {
  if (apiBaseFromQuery) {
    return [apiBaseFromQuery];
  }

  const candidates = [];
  const isDirectStaticPreview = ["63342", "5500"].includes(window.location.port);

  if (window.location.protocol === "file:") {
    candidates.push("http://localhost:18080/api/v1");
  } else if (isDirectStaticPreview) {
    candidates.push("http://localhost:18080/api/v1");
    candidates.push("/api/v1");
  } else {
    candidates.push("/api/v1");
    candidates.push("http://localhost:18080/api/v1");
  }

  return [...new Set(candidates)];
}

export function buildFetchError(error) {
  if (String(error.message || "").includes("Failed to fetch")) {
    return "请求被浏览器拦截或后端未启动。请优先用 Vite 开发服务器访问 http://localhost:18080。";
  }
  return error.message || "接口请求失败。";
}
