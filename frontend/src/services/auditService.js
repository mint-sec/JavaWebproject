const LOGIN_LOGS_KEY = "coldchain.frontend.audit.loginLogs";
const OPERATION_LOGS_KEY = "coldchain.frontend.audit.operationLogs";

const seedLoginLogs = [
  {
    id: "LGN-20260603-001",
    account: "admin",
    roleLabel: "管理员",
    result: "成功",
    ip: "127.0.0.1",
    detail: "本地开发环境登录",
    time: "2026-06-03 09:08:12",
  },
  {
    id: "LGN-20260603-002",
    account: "operator",
    roleLabel: "普通用户",
    result: "成功",
    ip: "127.0.0.1",
    detail: "业务用户登录工作台",
    time: "2026-06-03 09:16:45",
  },
  {
    id: "LGN-20260603-003",
    account: "guest-test",
    roleLabel: "未知",
    result: "失败",
    ip: "127.0.0.1",
    detail: "用户名或密码错误",
    time: "2026-06-03 09:19:08",
  },
];

const seedOperationLogs = [
  {
    id: "OPR-20260603-001",
    module: "用户管理",
    action: "修改用户角色",
    operator: "admin",
    target: "operator",
    result: "成功",
    detail: "将用户角色调整为普通用户",
    time: "2026-06-03 09:22:33",
  },
  {
    id: "OPR-20260603-002",
    module: "平台事项",
    action: "处理平台事项",
    operator: "admin",
    target: "ADM-ALT-02",
    result: "处理中",
    detail: "已收到新用户角色审核申请",
    time: "2026-06-03 09:28:05",
  },
];

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

function ensureLogs(key, seedList) {
  const existing = readJson(key, null);
  if (Array.isArray(existing)) {
    return existing;
  }
  writeJson(key, seedList);
  return seedList;
}

function appendLog(key, entry, prefix) {
  const logs = ensureLogs(key, []);
  const nextEntry = {
    id: `${prefix}-${Date.now()}`,
    time: nowLabel(),
    ...entry,
  };
  writeJson(key, [nextEntry, ...logs].slice(0, 60));
  return nextEntry;
}

export function listLoginLogs() {
  return ensureLogs(LOGIN_LOGS_KEY, seedLoginLogs);
}

export function listOperationLogs() {
  return ensureLogs(OPERATION_LOGS_KEY, seedOperationLogs);
}

export function appendLoginLog(entry) {
  return appendLog(LOGIN_LOGS_KEY, entry, "LGN");
}

export function appendOperationLog(entry) {
  return appendLog(OPERATION_LOGS_KEY, entry, "OPR");
}
