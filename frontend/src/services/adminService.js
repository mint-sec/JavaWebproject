import { listRegisteredUsers, updateStoredUser, USER_STATUS_ACTIVE, USER_STATUS_BANNED } from "./authService";
import { appendOperationLog, listLoginLogs, listOperationLogs } from "./auditService";

const VEHICLES_KEY = "coldchain.frontend.admin.vehicles";
const ALERTS_KEY = "coldchain.frontend.admin.alerts";

const VEHICLE_STATUS_OPTIONS = ["运输中", "待命中", "维修中", "已停用"];
const ALERT_LEVEL_OPTIONS = ["HIGH", "MEDIUM", "LOW"];
const ALERT_STATUS_OPTIONS = ["待处理", "处理中", "已处理"];
const ADMIN_OWNED_VEHICLE_IDS = new Set(["CC-VA-01", "CC-VA-02", "CC-VA-03", "CC-VA-04", "CC-VA-05"]);

const seedVehicles = [
  {
    vehicleId: "CC-VA-01",
    cargoName: "疫苗",
    status: "运输中",
    driver: "刘鹏",
    route: "北京仓库 -> 市医院",
    ownerUserId: "USR-ADMIN-001",
    ownerName: "admin",
    updatedAt: "2026-06-03 09:25:00",
  },
  {
    vehicleId: "CC-VA-02",
    cargoName: "药品",
    status: "待命中",
    driver: "王杰",
    route: "区域仓 -> 门诊 A",
    ownerUserId: "USR-OPS-001",
    ownerName: "operator",
    updatedAt: "2026-06-03 09:10:00",
  },
  {
    vehicleId: "CC-VA-03",
    cargoName: "生物制剂",
    status: "维修中",
    driver: "陈宇",
    route: "北区冷库 -> 疾控中心",
    ownerUserId: "USR-ADMIN-001",
    ownerName: "admin",
    updatedAt: "2026-06-03 08:40:00",
  },
  {
    vehicleId: "CC-VA-04",
    cargoName: "疫苗",
    status: "运输中",
    driver: "赵峰",
    route: "中心冷库 -> 社区接种点 A",
    ownerUserId: "USR-ADMIN-001",
    ownerName: "admin",
    updatedAt: "2026-06-03 08:55:00",
  },
  {
    vehicleId: "CC-VA-05",
    cargoName: "疫苗",
    status: "运输中",
    driver: "周凯",
    route: "中心冷库 -> 社区接种点 B",
    ownerUserId: "USR-ADMIN-001",
    ownerName: "admin",
    updatedAt: "2026-06-03 09:05:00",
  },
];

const seedAlerts = [
  {
    id: "ADM-ALT-01",
    title: "高温风险待处理",
    level: "HIGH",
    detail: "车辆 CC-VA-01 的应急改道方案仍待管理员确认。",
    vehicleId: "CC-VA-01",
    owner: "operator",
    ownerUserId: "USR-OPS-001",
    status: "待处理",
    note: "",
    domain: "BUSINESS",
  },
  {
    id: "ADM-ALT-02",
    title: "用户审核待处理",
    level: "MEDIUM",
    detail: "一名新注册操作员正在等待角色审核。",
    vehicleId: "N/A",
    owner: "系统管理员",
    ownerUserId: "USR-ADMIN-001",
    status: "处理中",
    note: "已收到申请，等待复核。",
    domain: "PLATFORM",
  },
  {
    id: "ADM-ALT-03",
    title: "运输计划待确认",
    level: "LOW",
    detail: "今日冷链配送计划已生成，待管理员确认后下发。",
    vehicleId: "CC-VA-01",
    owner: "admin",
    ownerUserId: "USR-ADMIN-001",
    status: "待处理",
    note: "",
    domain: "BUSINESS",
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

function applyVehicleOwnershipPolicy(vehicle) {
  if (!vehicle?.vehicleId) {
    return vehicle;
  }

  if (ADMIN_OWNED_VEHICLE_IDS.has(vehicle.vehicleId)) {
    return {
      ...vehicle,
      ownerUserId: "USR-ADMIN-001",
      ownerName: "admin",
    };
  }

  return vehicle;
}

function mergeVehiclesWithSeeds(existingVehicles) {
  const existingList = Array.isArray(existingVehicles) ? existingVehicles : [];
  const vehicleMap = new Map(existingList.map((item) => [item.vehicleId, item]));

  seedVehicles.forEach((seedVehicle) => {
    const existingVehicle = vehicleMap.get(seedVehicle.vehicleId);
    vehicleMap.set(
      seedVehicle.vehicleId,
      applyVehicleOwnershipPolicy({
        ...seedVehicle,
        ...existingVehicle,
      }),
    );
  });

  return Array.from(vehicleMap.values()).map(applyVehicleOwnershipPolicy);
}

function ensureVehicles() {
  const existing = readJson(VEHICLES_KEY, null);
  if (Array.isArray(existing)) {
    const mergedVehicles = mergeVehiclesWithSeeds(existing);
    if (JSON.stringify(mergedVehicles) !== JSON.stringify(existing)) {
      writeJson(VEHICLES_KEY, mergedVehicles);
    }
    return mergedVehicles;
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

function normalizeVehicle(vehicle) {
  return {
    ...vehicle,
    ownerName: vehicle.ownerName || "未分配",
  };
}

function getOwnedVehicles(currentUser) {
  return ensureVehicles()
    .filter((item) => item.ownerUserId === currentUser.userId)
    .map(normalizeVehicle);
}

function getOwnedAlerts(currentUser) {
  return ensureAlerts()
    .filter((item) => item.domain !== "PLATFORM" && item.ownerUserId === currentUser.userId)
    .map(normalizeAlert);
}

function createVehicleRecord(payload, ownerInfo = {}) {
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

  const nextVehicle = normalizeVehicle({
    vehicleId,
    cargoName,
    status,
    driver,
    route,
    ownerUserId: ownerInfo.ownerUserId || "",
    ownerName: ownerInfo.ownerName || "",
    updatedAt: nowLabel(),
  });

  saveVehicles([...vehicles, nextVehicle]);
  return nextVehicle;
}

function updateVehicleRecord(originalVehicleId, payload, guard) {
  const vehicles = ensureVehicles();
  const index = vehicles.findIndex((item) => item.vehicleId === originalVehicleId);
  if (index < 0) {
    throw new Error("未找到对应车辆。");
  }

  const currentVehicle = vehicles[index];
  if (typeof guard === "function") {
    guard(currentVehicle);
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

  const nextVehicle = normalizeVehicle({
    ...currentVehicle,
    vehicleId,
    cargoName,
    status,
    driver,
    route,
    updatedAt: nowLabel(),
  });

  const nextVehicles = [...vehicles];
  nextVehicles[index] = nextVehicle;
  saveVehicles(nextVehicles);
  return nextVehicle;
}

function deleteVehicleRecord(vehicleId, guard) {
  const vehicles = ensureVehicles();
  const target = vehicles.find((item) => item.vehicleId === vehicleId);
  if (!target) {
    throw new Error("未找到对应车辆。");
  }
  if (typeof guard === "function") {
    guard(target);
  }
  saveVehicles(vehicles.filter((item) => item.vehicleId !== vehicleId));
}

function updateAlertRecord(alertId, payload, guard) {
  const alerts = ensureAlerts();
  const index = alerts.findIndex((item) => item.id === alertId);
  if (index < 0) {
    throw new Error("未找到对应告警。");
  }

  const currentAlert = alerts[index];
  if (typeof guard === "function") {
    guard(currentAlert);
  }

  const owner = String(payload.owner || "").trim();
  const status = String(payload.status || "").trim();
  const level = String(payload.level || "").trim();
  const note = String(payload.note || "").trim();

  if (!owner || !status || !level) {
    throw new Error("请完整填写告警处理信息。");
  }

  const nextAlert = normalizeAlert({
    ...currentAlert,
    owner,
    status,
    level,
    note,
    handledAt: status === "已处理" ? nowLabel() : currentAlert.handledAt || "",
  });

  const nextAlerts = [...alerts];
  nextAlerts[index] = nextAlert;
  saveAlerts(nextAlerts);
  return nextAlert;
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

export function listOwnedVehicleIds(currentUser) {
  return getOwnedVehicles(currentUser).map((item) => item.vehicleId);
}

function buildServiceMonitors() {
  const checkedAt = nowLabel();

  return [
    {
      id: "service-frontend",
      name: "前端工作台",
      status: "正常",
      tone: "normal",
      latency: "12 ms",
      source: "本地页面",
      checkedAt,
      detail: "页面路由、状态同步与本地数据服务运行正常。",
    },
    {
      id: "service-backend",
      name: "后端接口服务",
      status: "待联调",
      tone: "pending",
      latency: "--",
      source: "/api/v1",
      checkedAt,
      detail: "等待真实服务健康检查接口接入，当前展示为前端预留状态。",
    },
    {
      id: "service-algorithm",
      name: "算法数据服务",
      status: "观察中",
      tone: "warning",
      latency: "--",
      source: "算法产数链路",
      checkedAt,
      detail: "当前监控大屏支持前端模拟数据，真实实时产数链路待与后端联调。",
    },
    {
      id: "service-database",
      name: "数据库写入链路",
      status: "待联调",
      tone: "pending",
      latency: "--",
      source: "MySQL / 持久化",
      checkedAt,
      detail: "车辆、告警与遥测历史落库状态等待后端提供真实指标。",
    },
  ];
}

function buildLogCenterCards(loginLogs, operationLogs) {
  return [
    {
      label: "登录日志总数",
      value: String(loginLogs.length),
      detail: "展示最近 60 条登录相关记录",
    },
    {
      label: "今日成功登录",
      value: String(loginLogs.filter((item) => item.result === "成功").length),
      detail: "包含注册后自动登录",
    },
    {
      label: "操作日志总数",
      value: String(operationLogs.length),
      detail: "展示最近 60 条平台操作记录",
    },
    {
      label: "敏感操作数",
      value: String(operationLogs.filter((item) => item.module === "用户管理").length),
      detail: "重点关注角色、状态、权限等变更",
    },
  ];
}

export async function getAdminConsoleData(currentUser) {
  await wait();

  const users = listRegisteredUsers().map(normalizeUser);
  const alerts = ensureAlerts().map(normalizeAlert);
  const platformAlerts = alerts.filter((item) => item.domain === "PLATFORM");
  const loginLogs = listLoginLogs();
  const operationLogs = listOperationLogs();
  const serviceMonitors = buildServiceMonitors();
  const abnormalServiceCount = serviceMonitors.filter((item) => item.tone !== "normal").length;

  return {
    overviewCards: [
      {
        label: "平台用户数",
        value: String(users.filter((item) => item.status === USER_STATUS_ACTIVE).length),
        detail: "当前可登录账号数量",
      },
      {
        label: "管理员人数",
        value: String(users.filter((item) => item.role === "ADMIN").length),
        detail: "具备后台权限的账号数量",
      },
      {
        label: "普通用户数",
        value: String(users.filter((item) => item.role === "USER").length),
        detail: "当前业务侧可用账号数量",
      },
      {
        label: "待审平台事项",
        value: String(platformAlerts.filter((item) => item.status !== "已处理").length),
        detail: `当前管理员：${currentUser.displayName}`,
      },
      {
        label: "今日登录记录",
        value: String(loginLogs.length),
        detail: "可在日志中心查看登录详情",
      },
      {
        label: "监控关注服务",
        value: String(abnormalServiceCount),
        detail: abnormalServiceCount ? "存在待联调或观察中的服务" : "所有服务状态正常",
      },
    ],
    logCenterCards: buildLogCenterCards(loginLogs, operationLogs),
    users,
    loginLogs,
    operationLogs,
    serviceMonitors,
  };
}

export async function getBusinessWorkspaceData(currentUser) {
  await wait();

  const vehicles = getOwnedVehicles(currentUser);
  const alerts = getOwnedAlerts(currentUser);

  return {
    overviewCards: [
      {
        label: "我的车辆",
        value: String(vehicles.length),
        detail: "当前归属到本人名下的车辆",
      },
      {
        label: "待处理告警",
        value: String(alerts.filter((item) => item.status === "待处理").length),
        detail: "需要尽快跟进的业务告警",
      },
      {
        label: "处理中告警",
        value: String(alerts.filter((item) => item.status === "处理中").length),
        detail: "已接手但尚未闭环的告警",
      },
      {
        label: "已处理告警",
        value: String(alerts.filter((item) => item.status === "已处理").length),
        detail: `当前责任人：${currentUser.displayName}`,
      },
    ],
    vehicles,
    alerts,
  };
}

export async function updateUserManagement(userId, payload, actor = null) {
  await wait();
  const nextUser = normalizeUser(updateStoredUser(userId, payload));
  appendOperationLog({
    module: "用户管理",
    action: "修改用户角色/状态",
    operator: actor?.displayName || "admin",
    target: nextUser.username,
    result: "成功",
    detail: `角色调整为 ${nextUser.roleLabel}，状态调整为 ${nextUser.status}`,
  });
  return nextUser;
}

export async function createVehicle(payload) {
  await wait();
  return createVehicleRecord(payload);
}

export async function createVehicleForUser(currentUser, payload) {
  await wait();
  return createVehicleRecord(payload, {
    ownerUserId: currentUser.userId,
    ownerName: currentUser.displayName,
  });
}

export async function updateVehicle(originalVehicleId, payload) {
  await wait();
  return updateVehicleRecord(originalVehicleId, payload);
}

export async function updateVehicleForUser(currentUser, originalVehicleId, payload) {
  await wait();
  return updateVehicleRecord(originalVehicleId, payload, (currentVehicle) => {
    if (currentVehicle.ownerUserId !== currentUser.userId) {
      throw new Error("只能编辑自己负责的车辆。");
    }
  });
}

export async function deleteVehicle(vehicleId) {
  await wait();
  deleteVehicleRecord(vehicleId);
}

export async function deleteVehicleForUser(currentUser, vehicleId) {
  await wait();
  deleteVehicleRecord(vehicleId, (currentVehicle) => {
    if (currentVehicle.ownerUserId !== currentUser.userId) {
      throw new Error("只能删除自己负责的车辆。");
    }
  });
}

export async function updateAlertManagement(alertId, payload) {
  await wait();
  return updateAlertRecord(alertId, payload);
}

export async function updateAlertForUser(currentUser, alertId, payload) {
  await wait();
  return updateAlertRecord(alertId, payload, (currentAlert) => {
    if (currentAlert.domain === "PLATFORM") {
      throw new Error("平台事项不能在用户工作台中处理。");
    }
    if (currentAlert.ownerUserId !== currentUser.userId) {
      throw new Error("只能处理分配给自己的告警。");
    }
  });
}
