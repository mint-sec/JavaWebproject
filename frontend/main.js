const SAFE_MIN = 2;
const SAFE_MAX = 8;

const routePoints = [
  { x: 88, y: 280, name: "配送中心", type: "origin" },
  { x: 220, y: 214, name: "医院 A", type: "waypoint" },
  { x: 360, y: 244, name: "疾控站 B", type: "waypoint" },
  { x: 520, y: 176, name: "社区门诊 C", type: "waypoint" },
  { x: 628, y: 120, name: "医院 D", type: "waypoint" },
];

const snapshots = [
  {
    time: "09:00",
    fullTime: "2026-05-13 09:00:00",
    temperature: 4.6,
    humidity: 66,
    speed: 48,
    doorOpen: false,
    risk: "低风险",
    remainingKm: 26.8,
    eta: "10:20",
    trend: "温度平稳",
    routeStatus: "按原计划配送",
    alert: {
      level: "low",
      title: "运输状态正常",
      detail: "温度处于安全温区内，车厢制冷系统运行稳定。",
      suggestion: "继续按既定路线配送，并保持 5 分钟级温度采样。",
    },
  },
  {
    time: "09:05",
    fullTime: "2026-05-13 09:05:00",
    temperature: 4.9,
    humidity: 65,
    speed: 46,
    doorOpen: false,
    risk: "低风险",
    remainingKm: 24.9,
    eta: "10:18",
    trend: "轻微升温",
    routeStatus: "按原计划配送",
    alert: {
      level: "low",
      title: "温度轻微波动",
      detail: "温度较上一采样点略升，但仍在可控区间内。",
      suggestion: "继续观察接下来两个采样点的变化趋势。",
    },
  },
  {
    time: "09:10",
    fullTime: "2026-05-13 09:10:00",
    temperature: 5.4,
    humidity: 66,
    speed: 42,
    doorOpen: false,
    risk: "低风险",
    remainingKm: 22.2,
    eta: "10:15",
    trend: "连续升温",
    routeStatus: "关注制冷波动",
    alert: {
      level: "medium",
      title: "趋势预警触发",
      detail: "连续 3 个采样点升温，存在提前预警信号。",
      suggestion: "检查车门状态与制冷设备负载，必要时联系调度中心。",
    },
  },
  {
    time: "09:15",
    fullTime: "2026-05-13 09:15:00",
    temperature: 6.1,
    humidity: 68,
    speed: 39,
    doorOpen: true,
    risk: "中风险",
    remainingKm: 20.5,
    eta: "10:14",
    trend: "开门导致升温",
    routeStatus: "优先送最近站点",
    alert: {
      level: "medium",
      title: "卸货开门温升",
      detail: "车门开启造成温度快速上升，短时风险增加。",
      suggestion: "缩短开门时长，完成站点作业后立即恢复制冷。",
    },
  },
  {
    time: "09:20",
    fullTime: "2026-05-13 09:20:00",
    temperature: 6.8,
    humidity: 69,
    speed: 37,
    doorOpen: false,
    risk: "中风险",
    remainingKm: 16.8,
    eta: "10:12",
    trend: "12 分钟后可能越界",
    routeStatus: "建议优先送医院 A",
    alert: {
      level: "medium",
      title: "预测型预警",
      detail: "若继续当前趋势，未来 12 分钟温度可能突破 8°C。",
      suggestion: "优先完成最近高敏货物配送，减少暴露时间。",
    },
  },
  {
    time: "09:25",
    fullTime: "2026-05-13 09:25:00",
    temperature: 7.5,
    humidity: 70,
    speed: 35,
    doorOpen: false,
    risk: "高风险",
    remainingKm: 13.4,
    eta: "10:11",
    trend: "逼近上限",
    routeStatus: "候选方案: 改道冷库",
    alert: {
      level: "high",
      title: "高风险临界告警",
      detail: "疫苗车厢温度接近安全上限，剩余路线较长。",
      suggestion: "比较最近冷库改道方案与继续配送方案的综合成本。",
    },
  },
];

const state = {
  cursor: snapshots.length - 1,
  history: snapshots.map((snapshot) => ({
    time: snapshot.time,
    temperature: snapshot.temperature,
  })),
  alerts: snapshots.slice(-4).reverse().map((snapshot) => snapshot.alert),
};

const refs = {
  vehicleId: document.querySelector("#vehicle-id"),
  systemTime: document.querySelector("#system-time"),
  summaryGrid: document.querySelector("#summary-grid"),
  routeMap: document.querySelector("#route-map"),
  routeStatus: document.querySelector("#route-status"),
  routeFooter: document.querySelector("#route-footer"),
  temperatureChart: document.querySelector("#temperature-chart"),
  temperatureState: document.querySelector("#temperature-state"),
  currentTemperature: document.querySelector("#current-temperature"),
  trendCopy: document.querySelector("#trend-copy"),
  alertsList: document.querySelector("#alerts-list"),
  alertCount: document.querySelector("#alert-count"),
};

function getCurrentSnapshot() {
  return snapshots[state.cursor];
}

function nextSnapshot() {
  state.cursor = (state.cursor + 1) % snapshots.length;
  const current = getCurrentSnapshot();
  state.history = [...state.history.slice(1), { time: current.time, temperature: current.temperature }];
  state.alerts = [current.alert, ...state.alerts].slice(0, 4);
  render();
}

function riskToTone(level) {
  if (level === "高风险") {
    return "danger";
  }
  if (level === "中风险") {
    return "warn";
  }
  return "";
}

function renderSummary(snapshot) {
  const cards = [
    { label: "当前温度", value: `${snapshot.temperature.toFixed(1)}°C`, extra: snapshot.trend },
    { label: "车厢湿度", value: `${snapshot.humidity}%`, extra: snapshot.doorOpen ? "车门开启中" : "车门关闭" },
    { label: "配送速度", value: `${snapshot.speed} km/h`, extra: `预计到达 ${snapshot.eta}` },
    { label: "风险等级", value: snapshot.risk, extra: `剩余路程 ${snapshot.remainingKm.toFixed(1)} km` },
  ];

  refs.summaryGrid.innerHTML = cards
    .map(
      (card) => `
        <article class="summary-card">
          <span>${card.label}</span>
          <strong>${card.value}</strong>
          <small>${card.extra}</small>
        </article>
      `,
    )
    .join("");
}

function renderRouteMap(snapshot) {
  const progress = state.cursor / (snapshots.length - 1 || 1);
  const routePath = routePoints.map((point) => `${point.x},${point.y}`).join(" ");
  const vehiclePosition = interpolateRoutePoint(progress);

  refs.routeMap.innerHTML = `
    <defs>
      <linearGradient id="routeGlow" x1="0%" x2="100%">
        <stop offset="0%" stop-color="#57d3ff" />
        <stop offset="100%" stop-color="#19d3a6" />
      </linearGradient>
      <filter id="dotGlow">
        <feGaussianBlur stdDeviation="4" result="blur" />
        <feMerge>
          <feMergeNode in="blur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>
    </defs>
    <rect x="0" y="0" width="720" height="380" rx="24" fill="transparent"></rect>
    ${Array.from({ length: 9 }, (_, index) => `<line x1="0" y1="${index * 48}" x2="720" y2="${index * 48}" stroke="rgba(255,255,255,0.06)" />`).join("")}
    ${Array.from({ length: 16 }, (_, index) => `<line x1="${index * 48}" y1="0" x2="${index * 48}" y2="380" stroke="rgba(255,255,255,0.05)" />`).join("")}
    <polyline points="${routePath}" fill="none" stroke="url(#routeGlow)" stroke-width="8" stroke-linecap="round" stroke-linejoin="round" opacity="0.22"></polyline>
    <polyline points="${routePath}" fill="none" stroke="url(#routeGlow)" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"></polyline>
    ${routePoints
      .map(
        (point) => `
          <g>
            <circle cx="${point.x}" cy="${point.y}" r="${point.type === "origin" ? 14 : 10}" fill="${point.type === "origin" ? "#57d3ff" : "#ffb347"}" filter="url(#dotGlow)" />
            <circle cx="${point.x}" cy="${point.y}" r="${point.type === "origin" ? 7 : 5}" fill="#07151d" />
            <text x="${point.x + 14}" y="${point.y - 16}" fill="#eff7fa" font-size="14">${point.name}</text>
          </g>
        `,
      )
      .join("")}
    <g>
      <circle cx="${vehiclePosition.x}" cy="${vehiclePosition.y}" r="18" fill="rgba(25,211,166,0.22)"></circle>
      <circle cx="${vehiclePosition.x}" cy="${vehiclePosition.y}" r="10" fill="#19d3a6"></circle>
      <text x="${vehiclePosition.x + 16}" y="${vehiclePosition.y + 5}" fill="#eff7fa" font-size="14">冷链车 CC-VA-01</text>
    </g>
  `;

  refs.routeStatus.className = `pill ${riskToTone(snapshot.risk)}`.trim();
  refs.routeStatus.textContent = snapshot.routeStatus;
  refs.routeFooter.innerHTML = `
    <div>
      <span>当前位置更新时间</span>
      <strong>${snapshot.fullTime}</strong>
    </div>
    <div>
      <span>剩余路程</span>
      <strong>${snapshot.remainingKm.toFixed(1)} km</strong>
    </div>
    <div>
      <span>调度建议</span>
      <strong>${snapshot.routeStatus}</strong>
    </div>
  `;
}

function interpolateRoutePoint(progress) {
  if (progress <= 0) {
    return routePoints[0];
  }
  if (progress >= 1) {
    return routePoints[routePoints.length - 1];
  }

  const segments = routePoints.length - 1;
  const position = progress * segments;
  const index = Math.floor(position);
  const ratio = position - index;
  const start = routePoints[index];
  const end = routePoints[index + 1];

  return {
    x: start.x + (end.x - start.x) * ratio,
    y: start.y + (end.y - start.y) * ratio,
  };
}

function renderTemperatureChart(snapshot) {
  const width = 720;
  const height = 360;
  const padding = { top: 24, right: 24, bottom: 48, left: 48 };
  const innerWidth = width - padding.left - padding.right;
  const innerHeight = height - padding.top - padding.bottom;
  const min = 0;
  const max = 10;

  const xStep = innerWidth / (state.history.length - 1 || 1);
  const toY = (value) => padding.top + innerHeight - ((value - min) / (max - min)) * innerHeight;
  const points = state.history
    .map((item, index) => `${padding.left + index * xStep},${toY(item.temperature)}`)
    .join(" ");
  const areaPoints = `${padding.left},${padding.top + innerHeight} ${points} ${padding.left + innerWidth},${padding.top + innerHeight}`;
  const safeTop = toY(SAFE_MAX);
  const safeHeight = toY(SAFE_MIN) - safeTop;
  const currentX = padding.left + innerWidth;
  const currentY = toY(snapshot.temperature);

  refs.temperatureChart.innerHTML = `
    <defs>
      <linearGradient id="tempArea" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stop-color="rgba(87,211,255,0.38)" />
        <stop offset="100%" stop-color="rgba(87,211,255,0.02)" />
      </linearGradient>
    </defs>
    <rect x="0" y="0" width="${width}" height="${height}" fill="transparent"></rect>
    <rect x="${padding.left}" y="${safeTop}" width="${innerWidth}" height="${safeHeight}" fill="rgba(25,211,166,0.09)"></rect>
    ${Array.from({ length: 6 }, (_, index) => {
      const value = min + ((max - min) / 5) * index;
      const y = toY(value);
      return `
        <line x1="${padding.left}" y1="${y}" x2="${padding.left + innerWidth}" y2="${y}" stroke="rgba(255,255,255,0.08)" />
        <text x="8" y="${y + 5}" fill="#88a5b0" font-size="12">${value.toFixed(0)}°C</text>
      `;
    }).join("")}
    <polygon points="${areaPoints}" fill="url(#tempArea)"></polygon>
    <polyline points="${points}" fill="none" stroke="#57d3ff" stroke-width="4" stroke-linejoin="round" stroke-linecap="round"></polyline>
    ${state.history
      .map((item, index) => {
        const x = padding.left + index * xStep;
        const y = toY(item.temperature);
        return `
          <circle cx="${x}" cy="${y}" r="5" fill="#07151d" stroke="#57d3ff" stroke-width="3"></circle>
          <text x="${x - 14}" y="${height - 16}" fill="#88a5b0" font-size="12">${item.time}</text>
        `;
      })
      .join("")}
    <circle cx="${currentX}" cy="${currentY}" r="7" fill="#19d3a6"></circle>
    <line x1="${padding.left}" y1="${padding.top + innerHeight}" x2="${padding.left + innerWidth}" y2="${padding.top + innerHeight}" stroke="rgba(255,255,255,0.12)" />
  `;

  refs.temperatureState.className = `pill ${riskToTone(snapshot.risk)}`.trim();
  refs.temperatureState.textContent = snapshot.trend;
  refs.currentTemperature.textContent = `${snapshot.temperature.toFixed(1)}°C`;
  refs.trendCopy.textContent = snapshot.trend;
}

function renderAlerts() {
  refs.alertCount.textContent = `${state.alerts.length} 条告警`;
  refs.alertsList.innerHTML = state.alerts
    .map(
      (alert, index) => `
        <article class="alert-card">
          <header>
            <h3>${alert.title}</h3>
            <span class="alert-tag ${alert.level}">${levelLabel(alert.level)}</span>
          </header>
          <p>${alert.detail}</p>
          <footer>
            <strong>${String(index + 1).padStart(2, "0")}</strong>
            ${alert.suggestion}
          </footer>
        </article>
      `,
    )
    .join("");
}

function levelLabel(level) {
  if (level === "high") {
    return "高风险";
  }
  if (level === "medium") {
    return "中风险";
  }
  return "低风险";
}

function render() {
  const current = getCurrentSnapshot();
  refs.vehicleId.textContent = "CC-VA-01";
  refs.systemTime.textContent = current.fullTime.split(" ")[1];
  renderSummary(current);
  renderRouteMap(current);
  renderTemperatureChart(current);
  renderAlerts();
}

render();
window.setInterval(nextSnapshot, 2500);
