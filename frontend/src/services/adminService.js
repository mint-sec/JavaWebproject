import { listRegisteredUsers, updateStoredUser, USER_STATUS_ACTIVE, USER_STATUS_BANNED } from "./authService";

const VEHICLES_KEY = "coldchain.frontend.admin.vehicles";
const ALERTS_KEY = "coldchain.frontend.admin.alerts";

const VEHICLE_STATUS_OPTIONS = ["运输中", "待命中", "维修中", "已停用"];
const ALERT_LEVEL_OPTIONS = ["HIGH", "MEDIUM", "LOW"];
const ALERT_STATUS_OPTIONS = ["待处理", "处理中", "已处理"];

const seedVehicles = [
  {
    vehicleId: "CC-VA-01",
    cargoName: "疫苗",
    status: "运输中",
    driver: "刘鹏",
    route: "北京仓库 -> 市医院",
    updatedAt: "2026-06-03 09:25:00",
  },
  {
    vehicleId: "CC-VA-02",
    cargoName: "药品",
    status: "待命中",
    driver: "王杰",
    route: "区域仓 -> 门诊 A",
    updatedAt: "2026-06-03 09:10:00",
  },
  {
    vehicleId: "CC-VA-03",
    cargoName: "生物制剂",
    status: "维修中",
    driver: "陈宇",
    route: "北区冷库 -> 疾控中心",
    updatedAt: "2026-06-03 08:40:00",
  },
];

const seedAlerts = [
  {
    id: "ADM-ALT-01",
    title: "高温风险待处理",
    level: "HIGH",
    detail: "车辆 CC-VA-01 的应急改道方案仍待管理员确认。",
    vehicleId: "CC-VA-01",
    owner: "调度负责人",
    status: "待处理",
    note: "",
  },
  {
    id: "ADM-ALT-02",
    title: "用户审核待处理",
    level: "MEDIUM",
    detail: "一名新注册操作员正在等待角色审核。",
    vehicleId: "N/A",
    owner: "系统管理员",
    status: "处理中",
    note: "已收到申请，等待复核。",
  },
  {
    id: "ADM-ALT-03",
    title: "运输计划待确认",
    level: "LOW",
    detail: "今日冷链配送计划已生成，待管理员确认后下发。",
    vehicleId: "平台任务",
    owner: "运营负责人",
    status: "待处理",
    note: "",
  },
];

function wait(ms = 180) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms);
  });
}

function readJson(key, fallback) {
  try {
    const raw = window.localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
}

function writeJson(key, value) {
  window.localStorage.setItem(key, JSON.stringify(value));
}

function nowLabel() {
  return new Date().toLocaleString("zh-CN", { hour12: false }).replace(/\//g, "-");
}

function ensureVehicles() {
  const existing = readJson(VEHICLES_KEY, null);
  if (Array.isArray(existing)) {
    return existing;
  }
  writeJson(VEHICLES_KEY, seedVehicles);
  return seedVehicles;
}

function ensureAlerts() {
  const existing = readJson(ALERTS_KEY, null);
  if (Array.isArray(existing)) {
    return existing;
  }
  writeJson(ALERTS_KEY, seedAlerts);
  return seedAlerts;
}

function saveVehicles(vehicles) {
  writeJson(VEHICLES_KEY, vehicles);
}

function saveAlerts(alerts) {
  writeJson(ALERTS_KEY, alerts);
}

function normalizeUser(user) {
  return {
    ...user,
    roleLabel: user.role === "ADMIN" ? "管理员" : "普通用户",
  };
}

function normalizeAlert(alert) {
  return {
    ...alert,
    levelLabel: alert.level === "HIGH" ? "高" : alert.level === "MEDIUM" ? "中" : "低",
  };
}

export function getUserRoleOptions() {
  return [
    { value: "USER", label: "普通用户" },
    { value: "ADMIN", label: "管理员" },
  ];
}

export function getUserStatusOptions() {
  return [
    { value: USER_STATUS_ACTIVE, label: USER_STATUS_ACTIVE },
    { value: USER_STATUS_BANNED, label: USER_STATUS_BANNED },
  ];
}

export function getVehicleStatusOptions() {
  return [...VEHICLE_STATUS_OPTIONS];
}

export function getAlertLevelOptions() {
  return [...ALERT_LEVEL_OPTIONS];
}

export function getAlertStatusOptions() {
  return [...ALERT_STATUS_OPTIONS];
}

export async function getAdminConsoleData(currentUser) {
  await wait();

  const users = listRegisteredUsers().map(normalizeUser);
  const vehicles = ensureVehicles();
  const alerts = ensureAlerts().map(normalizeAlert);

  return {
    overviewCards: [
      {
        label: "活跃用户数",
        value: String(users.filter((item) => item.status === USER_STATUS_ACTIVE).length),
        detail: "当前可登录账号数量",
      },
      {
        label: "管理员人数",
        value: String(users.filter((item) => item.role === "ADMIN").length),
        detail: "具备后台权限的账号数量",
      },
      {
        label: "车辆总数",
        value: String(vehicles.length),
        detail: "已纳入管理的车辆数量",
      },
      {
        label: "待处理告警",
        value: String(alerts.filter((item) => item.status !== "已处理").length),
        detail: `当前管理员：${currentUser.displayName}`,
      },
    ],
    users,
    vehicles,
    alerts,
  };
}

export async function updateUserManagement(userId, payload) {
  await wait();
  return normalizeUser(updateStoredUser(userId, payload));
}

export async function createVehicle(payload) {
  await wait();

  const vehicleId = String(payload.vehicleId || "").trim().toUpperCase();
  const cargoName = String(payload.cargoName || "").trim();
  const status = String(payload.status || "").trim();
  const driver = String(payload.driver || "").trim();
  const route = String(payload.route || "").trim();

  if (!vehicleId || !cargoName || !status || !driver || !route) {
    throw new Error("请完整填写车辆信息。");
  }

  const vehicles = ensureVehicles();
  if (vehicles.some((item) => item.vehicleId === vehicleId)) {
    throw new Error("车辆编号已存在。");
  }

  const nextVehicle = {
    vehicleId,
    cargoName,
    status,
    driver,
    route,
    updatedAt: nowLabel(),
  };

  saveVehicles([...vehicles, nextVehicle]);
  return nextVehicle;
}

export async function updateVehicle(originalVehicleId, payload) {
  await wait();

  const vehicles = ensureVehicles();
  const index = vehicles.findIndex((item) => item.vehicleId === originalVehicleId);
  if (index < 0) {
    throw new Error("未找到对应车辆。");
  }

  const vehicleId = String(payload.vehicleId || "").trim().toUpperCase();
  const cargoName = String(payload.cargoName || "").trim();
  const status = String(payload.status || "").trim();
  const driver = String(payload.driver || "").trim();
  const route = String(payload.route || "").trim();

  if (!vehicleId || !cargoName || !status || !driver || !route) {
    throw new Error("请完整填写车辆信息。");
  }

  if (vehicleId !== originalVehicleId && vehicles.some((item) => item.vehicleId === vehicleId)) {
    throw new Error("新的车辆编号已存在。");
  }

  const nextVehicle = {
    ...vehicles[index],
    vehicleId,
    cargoName,
    status,
    driver,
    route,
    updatedAt: nowLabel(),
  };

  const nextVehicles = [...vehicles];
  nextVehicles[index] = nextVehicle;
  saveVehicles(nextVehicles);
  return nextVehicle;
}

export async function deleteVehicle(vehicleId) {
  await wait();

  const vehicles = ensureVehicles();
  const nextVehicles = vehicles.filter((item) => item.vehicleId !== vehicleId);
  if (nextVehicles.length === vehicles.length) {
    throw new Error("未找到对应车辆。");
  }
  saveVehicles(nextVehicles);
}

export async function updateAlertManagement(alertId, payload) {
  await wait();

  const alerts = ensureAlerts();
  const index = alerts.findIndex((item) => item.id === alertId);
  if (index < 0) {
    throw new Error("未找到对应告警。");
  }

  const owner = String(payload.owner || "").trim();
  const status = String(payload.status || "").trim();
  const level = String(payload.level || "").trim();
  const note = String(payload.note || "").trim();

  if (!owner || !status || !level) {
    throw new Error("请完整填写告警处理信息。");
  }

  const nextAlert = {
    ...alerts[index],
    owner,
    status,
    level,
    note,
    handledAt: status === "已处理" ? nowLabel() : alerts[index].handledAt || "",
  };

  const nextAlerts = [...alerts];
  nextAlerts[index] = nextAlert;
  saveAlerts(nextAlerts);
  return normalizeAlert(nextAlert);
}
