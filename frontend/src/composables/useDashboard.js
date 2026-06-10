import { computed, reactive } from "vue";
import { browserMockVehicles, createBrowserMockDashboard } from "./dashboardMock";
import { getCurrentSession } from "../services/authService";
import {
  POLL_INTERVAL_MS,
  MAX_ALERTS,
  MAX_HISTORY,
  buildApiBaseCandidates,
  buildDashboardHistory,
  buildFetchError,
  clamp,
  deriveRiskLevel,
  formatClock,
  formatDateTime,
  getHighestAlertLevel,
  levelLabel,
  normalizeAlerts,
  normalizeTelemetry,
  normalizeVehicle,
  toneClass,
  vehicleStatusLabel,
} from "./dashboardUtils";

export function useDashboard(currentUser) {
  const apiBaseFromQuery = new URLSearchParams(window.location.search).get("apiBase");
  const apiBaseCandidates = buildApiBaseCandidates(apiBaseFromQuery);

  const state = reactive({
    vehicles: [],
    selectedVehicleKey: "",
    vehicleMeta: null,
    latestTelemetry: null,
    alerts: [],
    history: [],
    trajectory: [],
    chartFocusIndex: null,
    followLatest: true,
    focusedRecordTime: "",
    replayActive: false,
    isLoading: false,
    lastSyncAt: null,
    error: "",
    emptyMessage: "",
    dataSource: "backend",
    apiBase: "",
  });

  let pollingTimer = null;
  let replayTimer = null;

  const latestIndex = computed(() => Math.max(state.history.length - 1, 0));
  const activeIndex = computed(() => clamp(state.chartFocusIndex ?? latestIndex.value, 0, latestIndex.value));
  const activePoint = computed(() => {
    if (!state.history.length) {
      return null;
    }
    return state.history[activeIndex.value] || state.history[latestIndex.value];
  });
  const highestLevel = computed(() =>
    state.alerts.length ? getHighestAlertLevel(state.alerts) : deriveRiskLevel(state.vehicleMeta, state.latestTelemetry),
  );
  const sceneLabel = computed(() =>
    state.vehicleMeta?.cargoName ? `${state.vehicleMeta.cargoName}冷链运输` : "冷链运输",
  );
  const vehicleMetaLabel = computed(() => {
    if (!state.vehicleMeta) {
      return "等待载入车辆信息";
    }
    return `${state.vehicleMeta.plateNumber || "--"} · ${vehicleStatusLabel(state.vehicleMeta.status)} · ${state.vehicleMeta.cargoName}`;
  });
  const systemTimeLabel = computed(() => activePoint.value?.recordTime || "--");
  const recordTimeLabel = computed(() => {
    if (state.lastSyncAt) {
      return `页面刷新时间 ${formatDateTime(state.lastSyncAt)}`;
    }
    return "等待首轮同步";
  });
  const syncStatus = computed(() => {
    if (state.emptyMessage) {
      return "未配置";
    }
    if (state.error) {
      return "同步异常";
    }
    if (state.lastSyncAt) {
      return "运行中";
    }
    return "准备中";
  });
  const syncDetail = computed(() => {
    if (state.emptyMessage) {
      return state.emptyMessage;
    }
    if (state.error) {
      return buildFetchError({ message: state.error });
    }
    return `最近同步 ${state.lastSyncAt ? formatClock(state.lastSyncAt) : "--:--:--"}`;
  });
  const syncHeadline = computed(() => {
    if (state.emptyMessage) {
      return state.emptyMessage;
    }
    if (state.error) {
      return "数据请求失败，请检查前后端连接状态。";
    }
    if (state.isLoading) {
      return "正在更新最新监控数据...";
    }
    if (state.vehicleMeta) {
      return `${state.vehicleMeta.vehicleId} 当前风险 ${levelLabel(highestLevel.value)}`;
    }
    return "等待车辆数据";
  });
  const pollingTone = computed(() => {
    if (state.emptyMessage) {
      return "";
    }
    if (state.error) {
      return "danger";
    }
    if (state.isLoading) {
      return "";
    }
    return toneClass(highestLevel.value);
  });
  const pollingLabel = computed(() => {
    if (state.emptyMessage) {
      return "暂无车辆";
    }
    if (state.error) {
      return "连接异常";
    }
    if (state.isLoading) {
      return "同步中";
    }
    return state.followLatest ? "实时跟随中" : "手动查看历史中";
  });
  const summaryCards = computed(() => {
    if (!state.vehicleMeta || !state.latestTelemetry) {
      return [];
    }

    const highestAlert = state.alerts[0] || null;
    const riskTone = toneClass(highestLevel.value);
    return [
      {
        label: "当前温度",
        value: `${state.latestTelemetry.temperature.toFixed(1)}°C`,
        extra: state.latestTelemetry.trend || "温度趋势更新中",
        tone: riskTone,
      },
      {
        label: "车厢状态",
        value: state.latestTelemetry.doorOpen ? "开门中" : "已关闭",
        extra: `湿度 ${state.latestTelemetry.humidity.toFixed(0)}% · 外部 ${state.latestTelemetry.outsideTemp.toFixed(1)}°C`,
      },
      {
        label: "运行状态",
        value: `${state.latestTelemetry.speed.toFixed(1)} km/h`,
        extra: `剩余 ${state.latestTelemetry.remainingKm.toFixed(1)} km`,
      },
      {
        label: "风险等级",
        value: levelLabel(highestLevel.value),
        extra: highestAlert ? highestAlert.title : "当前暂无新增告警",
        tone: riskTone,
      },
      {
        label: "车辆状态",
        value: vehicleStatusLabel(state.vehicleMeta.status),
        extra: `${state.vehicleMeta.vehicleId} · 总路程 ${state.vehicleMeta.routeDistanceKm || 0} km`,
      },
    ];
  });
  const routeTone = computed(() => toneClass(highestLevel.value));
  const temperatureTone = computed(() => toneClass(deriveRiskLevel(state.vehicleMeta, activePoint.value)));

  async function initialize() {
    await resolveApiBase();
    await loadVehicles();
    await refreshDashboard();
    pollingTimer = window.setInterval(refreshDashboard, POLL_INTERVAL_MS);
  }

  function cleanup() {
    if (pollingTimer) {
      window.clearInterval(pollingTimer);
      pollingTimer = null;
    }
    stopReplay(false);
  }

  async function resolveApiBase() {
    let lastError = null;

    for (const candidate of apiBaseCandidates) {
      try {
        const response = await fetchJson(`${candidate}/vehicles`);
        if (Array.isArray(response.data)) {
          state.apiBase = candidate;
          state.dataSource = response.source;
          state.error = "";
          return;
        }
      } catch (error) {
        lastError = error;
      }
    }

    state.apiBase = "browser-mock";
    state.dataSource = "browser-mock";
    state.error = lastError ? buildFetchError(lastError) : "";
  }

  async function loadVehicles() {
    if (state.apiBase === "browser-mock") {
      state.vehicles = browserMockVehicles
        .filter((item) => item.ownerUserId === currentUser?.userId)
        .map((item) => normalizeVehicle(item));
    } else {
      const response = await fetchJson(`${state.apiBase}/vehicles`);
      state.vehicles = (Array.isArray(response.data) ? response.data : []).map(normalizeVehicle);
      state.dataSource = response.source;
    }

    if (!state.vehicles.length) {
      resetVehicleData();
      state.emptyMessage = "当前账号下暂无可监控车辆，请先到“我的车辆”中创建或维护车辆。";
      return;
    }

    state.emptyMessage = "";
    if (!state.selectedVehicleKey || !state.vehicles.some((item) => item.vehicleKey === state.selectedVehicleKey)) {
      state.selectedVehicleKey = state.vehicles[0].vehicleKey;
    }

    syncVehicleMeta();
  }

  async function refreshDashboard() {
    if (!state.selectedVehicleKey && state.apiBase !== "browser-mock") {
      return;
    }

    state.isLoading = true;

    try {
      let telemetryResponse;
      let alertsResponse;
      let dashboardResponse;

      if (state.apiBase === "browser-mock") {
        const browserMockVehicleId = state.vehicleMeta?.vehicleId || state.selectedVehicleKey;
        [telemetryResponse, alertsResponse] = await createBrowserMockDashboard(browserMockVehicleId, MAX_ALERTS);
      } else {
        [dashboardResponse, telemetryResponse] = await Promise.all([
          fetchJson(`${state.apiBase}/dashboard/vehicles/${encodeURIComponent(state.selectedVehicleKey)}`),
          fetchJson(`${state.apiBase}/vehicles/${encodeURIComponent(state.selectedVehicleKey)}/telemetry/latest`),
        ]);
        alertsResponse = {
          data: dashboardResponse.data?.alerts || [],
          source: dashboardResponse.source,
        };
      }

      syncVehicleMeta();
      state.latestTelemetry = normalizeTelemetry(telemetryResponse.data);
      state.alerts = normalizeAlerts(alertsResponse.data);
      state.dataSource =
        telemetryResponse.source === "browser-mock" || alertsResponse.source === "browser-mock"
          ? "browser-mock"
          : telemetryResponse.source === "mock" || alertsResponse.source === "mock"
            ? "mock"
            : "backend";

      if (state.apiBase === "browser-mock") {
        appendHistoryPoint(state.latestTelemetry);
      } else {
        applyDashboardSnapshot(dashboardResponse.data, state.latestTelemetry);
      }

      state.lastSyncAt = new Date();
      state.error = "";
      state.emptyMessage = "";
      applyFocusAfterHistoryRefresh();
    } catch (error) {
      state.error = error.message;
    } finally {
      state.isLoading = false;
    }
  }

  async function selectVehicle(nextVehicleKey) {
    stopReplay(false);
    state.selectedVehicleKey = nextVehicleKey;
    state.followLatest = true;
    state.focusedRecordTime = "";
    state.chartFocusIndex = null;
    state.latestTelemetry = null;
    state.alerts = [];
    state.history = [];
    state.trajectory = [];
    syncVehicleMeta();
    await refreshDashboard();
  }

  function refreshNow() {
    return refreshDashboard();
  }

  function focusLatest() {
    stopReplay(false);
    state.followLatest = true;
    state.chartFocusIndex = latestIndex.value;
    state.focusedRecordTime = state.history[latestIndex.value]?.recordTime || "";
  }

  function setActiveIndex(nextIndex) {
    stopReplay(false);
    state.chartFocusIndex = clamp(nextIndex, 0, latestIndex.value);
    state.followLatest = state.chartFocusIndex >= latestIndex.value;
    state.focusedRecordTime = state.history[state.chartFocusIndex]?.recordTime || "";
  }

  function toggleReplay() {
    if (state.history.length < 2) {
      return;
    }

    if (state.replayActive) {
      stopReplay(true);
      return;
    }

    stopReplay(false);
    state.replayActive = true;
    state.followLatest = false;
    state.chartFocusIndex = 0;
    state.focusedRecordTime = state.history[0]?.recordTime || "";

    replayTimer = window.setInterval(() => {
      if (state.chartFocusIndex >= latestIndex.value) {
        stopReplay(true);
        return;
      }
      state.chartFocusIndex += 1;
      state.focusedRecordTime = state.history[state.chartFocusIndex]?.recordTime || "";
    }, 900);
  }

  function stopReplay(resetToLatest) {
    if (replayTimer) {
      window.clearInterval(replayTimer);
      replayTimer = null;
    }
    state.replayActive = false;
    if (resetToLatest && state.history.length) {
      state.followLatest = true;
      state.chartFocusIndex = latestIndex.value;
      state.focusedRecordTime = state.history[latestIndex.value]?.recordTime || "";
    }
  }

  function appendHistoryPoint(telemetry) {
    const point = {
      ...telemetry,
      sampleTime: telemetry.recordTime?.split(" ")[1] || formatClock(new Date()),
    };

    const nextHistory = [...state.history];
    const lastPoint = nextHistory[nextHistory.length - 1];
    if (lastPoint?.recordTime === point.recordTime) {
      nextHistory[nextHistory.length - 1] = point;
    } else {
      nextHistory.push(point);
    }

    state.history = nextHistory.slice(-MAX_HISTORY);
    syncTrajectory();
  }

  function applyDashboardSnapshot(dashboard, latestTelemetry) {
    state.history = buildDashboardHistory(dashboard?.temperatureHistory, latestTelemetry, dashboard?.route).slice(-MAX_HISTORY);
    syncTrajectory();
  }

  function syncTrajectory() {
    state.trajectory = state.history.map((item) => ({
      lng: item.lng,
      lat: item.lat,
      sampleTime: item.sampleTime,
      recordTime: item.recordTime,
      remainingKm: item.remainingKm,
    }));
  }

  function applyFocusAfterHistoryRefresh() {
    if (!state.history.length) {
      state.chartFocusIndex = null;
      state.focusedRecordTime = "";
      return;
    }

    if (state.replayActive) {
      state.chartFocusIndex = clamp(state.chartFocusIndex ?? 0, 0, latestIndex.value);
      state.focusedRecordTime = state.history[state.chartFocusIndex]?.recordTime || "";
      return;
    }

    if (state.followLatest) {
      state.chartFocusIndex = latestIndex.value;
      state.focusedRecordTime = state.history[latestIndex.value]?.recordTime || "";
      return;
    }

    const targetTime = state.focusedRecordTime;
    const matchedIndex = targetTime ? state.history.findIndex((item) => item.recordTime === targetTime) : -1;
    if (matchedIndex >= 0) {
      state.chartFocusIndex = matchedIndex;
      return;
    }

    state.chartFocusIndex = clamp(state.chartFocusIndex ?? 0, 0, latestIndex.value);
    state.focusedRecordTime = state.history[state.chartFocusIndex]?.recordTime || "";
  }

  function syncVehicleMeta() {
    state.vehicleMeta = state.vehicles.find((item) => item.vehicleKey === state.selectedVehicleKey) || state.vehicles[0] || null;
  }

  function resetVehicleData() {
    state.selectedVehicleKey = "";
    state.vehicleMeta = null;
    state.latestTelemetry = null;
    state.alerts = [];
    state.history = [];
    state.trajectory = [];
    state.chartFocusIndex = null;
    state.followLatest = true;
    state.focusedRecordTime = "";
  }

  async function fetchJson(url) {
    const session = getCurrentSession();
    const headers = {
      Accept: "application/json",
    };

    if (session?.token) {
      headers.Authorization = `Bearer ${session.token}`;
    }

    const response = await fetch(url, {
      headers,
      cache: "no-store",
    });

    if (!response.ok) {
      let message = `请求失败：${response.status}`;
      try {
        const payload = await response.json();
        message = payload.message || message;
      } catch {
        // ignore
      }
      throw new Error(message);
    }

    const payload = await response.json();
    if (!payload.success) {
      throw new Error(payload.message || "接口返回失败");
    }

    return {
      data: payload.data,
      source: response.headers.get("x-coldchain-source") || "backend",
    };
  }

  return {
    state,
    activePoint,
    activeIndex,
    latestIndex,
    sceneLabel,
    vehicleMetaLabel,
    systemTimeLabel,
    recordTimeLabel,
    syncStatus,
    syncDetail,
    syncHeadline,
    pollingTone,
    pollingLabel,
    highestLevel,
    summaryCards,
    routeTone,
    temperatureTone,
    initialize,
    cleanup,
    selectVehicle,
    refreshNow,
    focusLatest,
    setActiveIndex,
    toggleReplay,
    toneClass,
    levelLabel,
  };
}
