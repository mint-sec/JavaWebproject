import { computed, reactive } from "vue";
import { browserMockVehicles, createBrowserMockDashboard } from "./dashboardMock";
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
  formatNumber,
  getHighestAlertLevel,
  levelLabel,
  normalizeAlerts,
  normalizeTelemetry,
  normalizeVehicle,
  toneClass,
  vehicleStatusLabel,
} from "./dashboardUtils";

const DEMO_STEPS = [
  {
    title: "总览启幕",
    description: "先展示当前车辆的状态卡片、接口状态和整体风险。",
    vehicleId: "CC-VA-01",
    replay: false,
  },
  {
    title: "风险切换",
    description: "切到开门波动车辆，突出告警与温度变化。",
    vehicleId: "CC-VA-03",
    replay: false,
  },
  {
    title: "高风险高亮",
    description: "切到高风险车辆，强化红色预警和风险区分。",
    vehicleId: "CC-VA-05",
    replay: false,
  },
  {
    title: "轨迹回放",
    description: "回到主车并启动轨迹回放，适合录屏收尾展示。",
    vehicleId: "CC-VA-01",
    replay: true,
  },
];

export function useDashboard() {
  const apiBaseFromQuery = new URLSearchParams(window.location.search).get("apiBase");
  const apiBaseCandidates = buildApiBaseCandidates(apiBaseFromQuery);

  const state = reactive({
    vehicles: [],
    vehicleId: "",
    vehicleMeta: null,
    latestTelemetry: null,
    alerts: [],
    history: [],
    trajectory: [],
    chartFocusIndex: null,
    replayActive: false,
    isLoading: false,
    lastSyncAt: null,
    error: "",
    dataSource: "backend",
    apiBase: "",
    demoMode: false,
    demoStage: 0,
    demoMessage: "点击自动演示，可按风险、告警和轨迹顺序轮播。",
  });

  let pollingTimer = null;
  let replayTimer = null;
  let demoTimer = null;
  let demoLock = false;

  const latestIndex = computed(() => Math.max(state.history.length - 1, 0));
  const activeIndex = computed(() => clamp(state.chartFocusIndex ?? latestIndex.value, 0, latestIndex.value));
  const activePoint = computed(() => {
    if (!state.history.length) {
      return null;
    }
    return state.history[activeIndex.value] || state.history[latestIndex.value];
  });
  const highestLevel = computed(() => (state.alerts.length ? getHighestAlertLevel(state.alerts) : deriveRiskLevel(state.vehicleMeta, state.latestTelemetry)));
  const mockSource = computed(() => state.dataSource === "mock" || state.dataSource === "browser-mock");
  const sceneLabel = computed(() => (state.vehicleMeta?.cargoName ? `${state.vehicleMeta.cargoName}冷链配送` : "疫苗冷链配送"));
  const vehicleMetaLabel = computed(() => {
    if (!state.vehicleMeta) {
      return "等待载入车辆信息...";
    }
    return `${state.vehicleMeta.plateNumber} · ${vehicleStatusLabel(state.vehicleMeta.status)} · ${state.vehicleMeta.cargoName}`;
  });
  const systemTimeLabel = computed(() => activePoint.value?.recordTime?.split(" ")[1] || "--:--:--");
  const recordTimeLabel = computed(() => (activePoint.value?.recordTime ? `后端记录时间 ${activePoint.value.recordTime}` : "后端时间待同步"));
  const syncStatus = computed(() => {
    if (state.error) {
      return "同步异常";
    }
    if (mockSource.value) {
      return "模拟数据中";
    }
    if (state.lastSyncAt) {
      return "接口已连通";
    }
    return "准备连接";
  });
  const syncDetail = computed(() => {
    if (state.error) {
      return buildFetchError({ message: state.error });
    }
    return `${mockSource.value ? "数据来源 前端本地 mock 兜底" : "数据来源 后端接口"} · 接口基址 ${state.apiBase || "待探测"} · 最近同步 ${state.lastSyncAt ? formatClock(state.lastSyncAt) : "--:--:--"}`;
  });
  const syncHeadline = computed(() => {
    if (state.error) {
      return "接口请求失败，请检查 Vite 代理或后端服务";
    }
    if (state.isLoading) {
      return "正在拉取最新数据...";
    }
    if (mockSource.value) {
      return "当前为前端本地 mock 兜底，字段结构已对齐后端接口";
    }
    if (state.vehicleMeta) {
      return `${state.vehicleMeta.vehicleId} 当前风险 ${levelLabel(highestLevel.value)}`;
    }
    return "等待车辆数据";
  });
  const pollingTone = computed(() => {
    if (state.error) {
      return "danger";
    }
    if (state.isLoading) {
      return "";
    }
    return toneClass(highestLevel.value);
  });
  const pollingLabel = computed(() => {
    if (state.error) {
      return "连接异常";
    }
    if (state.isLoading) {
      return "同步中";
    }
    return "实时轮询中";
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
        extra: state.latestTelemetry.trend || "温度趋势待分析",
        tone: riskTone,
      },
      {
        label: "车厢状态",
        value: state.latestTelemetry.doorOpen ? "开门中" : "已关门",
        extra: `湿度 ${formatNumber(state.latestTelemetry.humidity)}% · 外部 ${formatNumber(state.latestTelemetry.outsideTemp)}°C`,
      },
      {
        label: "运行状态",
        value: `${formatNumber(state.latestTelemetry.speed)} km/h`,
        extra: `剩余 ${formatNumber(state.latestTelemetry.remainingKm)} km`,
      },
      {
        label: "风险等级",
        value: levelLabel(highestLevel.value),
        extra: highestAlert ? highestAlert.title : "当前无新增告警",
        tone: riskTone,
      },
      {
        label: "车辆状态",
        value: vehicleStatusLabel(state.vehicleMeta.status),
        extra: `${state.vehicleMeta.plateNumber} · ${state.vehicleMeta.cargoName}`,
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
    stopDemoMode(false);
    stopReplay(false);
  }

  function stopDemoMode(resetStage) {
    if (demoTimer) {
      window.clearInterval(demoTimer);
      demoTimer = null;
    }
    state.demoMode = false;
    if (resetStage) {
      state.demoStage = 0;
      state.demoMessage = "点击自动演示，可按风险、告警和轨迹顺序轮播。";
    }
  }

  async function resolveApiBase() {
    let lastError = null;

    for (const candidate of apiBaseCandidates) {
      try {
        const response = await fetchJson(`${candidate}/vehicles`);
        if (Array.isArray(response.data) && response.data.length) {
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
      state.vehicles = browserMockVehicles;
    } else {
      const response = await fetchJson(`${state.apiBase}/vehicles`);
      state.vehicles = Array.isArray(response.data) ? response.data.map(normalizeVehicle) : [];
      state.dataSource = response.source;
    }

    if (!state.vehicles.length) {
      throw new Error("车辆列表为空，无法初始化页面。");
    }

    if (!state.vehicleId || !state.vehicles.some((item) => item.vehicleId === state.vehicleId)) {
      state.vehicleId = state.vehicles[0].vehicleId;
    }

    state.vehicleMeta = state.vehicles.find((item) => item.vehicleId === state.vehicleId) || state.vehicles[0];
  }

  async function refreshDashboard() {
    if (!state.vehicleId) {
      return;
    }

    state.isLoading = true;

    try {
      let telemetryResponse;
      let alertsResponse;
      let dashboardResponse;

      if (state.apiBase === "browser-mock") {
        [telemetryResponse, alertsResponse] = await createBrowserMockDashboard(state.vehicleId, MAX_ALERTS);
      } else {
        [dashboardResponse, telemetryResponse] = await Promise.all([
          fetchJson(`${state.apiBase}/dashboard/vehicles/${encodeURIComponent(state.vehicleId)}`),
          fetchJson(`${state.apiBase}/vehicles/${encodeURIComponent(state.vehicleId)}/telemetry/latest`),
        ]);
        alertsResponse = {
          data: dashboardResponse.data?.alerts || [],
          source: dashboardResponse.source,
        };
      }

      state.vehicleMeta = state.vehicles.find((item) => item.vehicleId === state.vehicleId) || state.vehicleMeta;
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

      if (state.chartFocusIndex == null || !state.replayActive) {
        state.chartFocusIndex = latestIndex.value;
      }
    } catch (error) {
      state.error = error.message;
    } finally {
      state.isLoading = false;
    }
  }

  async function selectVehicle(nextVehicleId, preserveDemoMode = false) {
    if (!preserveDemoMode) {
      stopDemoMode(false);
    }
    stopReplay(false);
    state.vehicleId = nextVehicleId;
    state.vehicleMeta = state.vehicles.find((item) => item.vehicleId === state.vehicleId) || null;
    state.latestTelemetry = null;
    state.alerts = [];
    state.history = [];
    state.trajectory = [];
    state.chartFocusIndex = null;
    await refreshDashboard();
  }

  async function runDemoStep(stepIndex = state.demoStage) {
    if (demoLock) {
      return;
    }

    const step = DEMO_STEPS[stepIndex % DEMO_STEPS.length];
    if (!step) {
      return;
    }

    demoLock = true;
    try {
      state.demoMessage = `${step.title} · ${step.description}`;
      await selectVehicle(step.vehicleId, true);
      focusLatest();

      if (step.replay) {
        toggleReplay();
      } else {
        stopReplay(false);
      }
    } finally {
      demoLock = false;
    }
  }

  async function startDemoMode() {
    if (state.demoMode) {
      return;
    }

    stopDemoMode(false);
    state.demoMode = true;
    state.demoStage = 0;
    await runDemoStep(0);

    demoTimer = window.setInterval(() => {
      state.demoStage = (state.demoStage + 1) % DEMO_STEPS.length;
      void runDemoStep(state.demoStage);
    }, 7000);
  }

  function toggleDemoMode() {
    if (state.demoMode) {
      stopDemoMode(true);
      return;
    }

    void startDemoMode();
  }

  function refreshNow() {
    return refreshDashboard();
  }

  function focusLatest() {
    stopReplay(true);
    state.chartFocusIndex = latestIndex.value;
  }

  function setActiveIndex(nextIndex) {
    stopReplay(false);
    state.chartFocusIndex = clamp(nextIndex, 0, latestIndex.value);
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
    state.chartFocusIndex = 0;

    replayTimer = window.setInterval(() => {
      if (state.chartFocusIndex >= latestIndex.value) {
        stopReplay(true);
        return;
      }
      state.chartFocusIndex += 1;
    }, 900);
  }

  function stopReplay(resetToLatest) {
    if (replayTimer) {
      window.clearInterval(replayTimer);
      replayTimer = null;
    }
    state.replayActive = false;
    if (resetToLatest && state.history.length) {
      state.chartFocusIndex = latestIndex.value;
    }
  }

  function appendHistoryPoint(telemetry) {
    const sampledAt = formatClock(new Date());
    const point = {
      ...telemetry,
      sampleTime: sampledAt,
    };

    state.history = [...state.history, point].slice(-MAX_HISTORY);
    state.trajectory = state.history.map((item) => ({
      lng: item.lng,
      lat: item.lat,
      sampleTime: item.sampleTime,
      recordTime: item.recordTime,
      remainingKm: item.remainingKm,
    }));

    if (!state.replayActive) {
      state.chartFocusIndex = latestIndex.value;
    }
  }

  function applyDashboardSnapshot(dashboard, latestTelemetry) {
    const history = buildDashboardHistory(dashboard?.temperatureHistory, latestTelemetry, dashboard?.route).slice(-MAX_HISTORY);

    state.history = history;
    state.trajectory = history.map((item) => ({
      lng: item.lng,
      lat: item.lat,
      sampleTime: item.sampleTime,
      recordTime: item.recordTime,
      remainingKm: item.remainingKm,
    }));

    if (!state.replayActive) {
      state.chartFocusIndex = latestIndex.value;
    }
  }

  async function fetchJson(url) {
    const response = await fetch(url, {
      headers: {
        Accept: "application/json",
      },
      cache: "no-store",
    });

    if (!response.ok) {
      throw new Error(`请求失败：${response.status}`);
    }

    const payload = await response.json();
    if (!payload.success) {
      throw new Error(payload.message || "接口返回失败。");
    }

    return {
      data: payload.data,
      source: response.headers.get("x-coldchain-source") || "backend",
    };
  }

  return {
    state,
    demoSteps: DEMO_STEPS,
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
    toggleDemoMode,
    refreshNow,
    focusLatest,
    setActiveIndex,
    toggleReplay,
    toneClass,
    levelLabel,
  };
}
