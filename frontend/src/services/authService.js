const USERS_KEY = "coldchain.frontend.auth.users";
const SESSION_KEY = "coldchain.frontend.auth.session";
const SESSION_EVENT = "coldchain-session-changed";

export const USER_STATUS_ACTIVE = "启用中";
export const USER_STATUS_BANNED = "已封禁";

const seedUsers = [
  {
    id: "USR-ADMIN-001",
    username: "admin",
    phone: "13800000001",
    email: "admin@coldchain.local",
    password: "Admin123!",
    displayName: "admin",
    role: "ADMIN",
    status: USER_STATUS_ACTIVE,
    origin: "系统账号",
  },
  {
    id: "USR-OPS-001",
    username: "operator",
    phone: "13800000002",
    email: "operator@coldchain.local",
    password: "Operator123!",
    displayName: "operator",
    role: "USER",
    status: USER_STATUS_ACTIVE,
    origin: "系统账号",
  },
];

function wait(ms = 220) {
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

function notifySessionChanged() {
  window.dispatchEvent(new CustomEvent(SESSION_EVENT));
}

function ensureUsers() {
  const existing = readJson(USERS_KEY, null);
  if (Array.isArray(existing) && existing.length) {
    return existing;
  }
  writeJson(USERS_KEY, seedUsers);
  return seedUsers;
}

function saveUsers(users) {
  writeJson(USERS_KEY, users);
}

function toPublicUser(user) {
  const { password, ...rest } = user;
  return {
    ...rest,
    roleLabel: user.role === "ADMIN" ? "管理员" : "普通用户",
  };
}

function toSession(user) {
  return {
    userId: user.id,
    username: user.username,
    displayName: user.displayName,
    role: user.role,
    roleLabel: user.role === "ADMIN" ? "管理员" : "普通用户",
    token: `local-session-${user.id}`,
    loggedInAt: new Date().toISOString(),
  };
}

function writeSession(session) {
  writeJson(SESSION_KEY, session);
  notifySessionChanged();
}

function syncCurrentSession(nextUser) {
  const currentSession = readJson(SESSION_KEY, null);
  if (!currentSession || currentSession.userId !== nextUser.id) {
    return;
  }

  if (nextUser.status !== USER_STATUS_ACTIVE) {
    window.localStorage.removeItem(SESSION_KEY);
    notifySessionChanged();
    return;
  }

  writeSession(toSession(nextUser));
}

export function getCurrentSession() {
  return readJson(SESSION_KEY, null);
}

export function onSessionChanged(listener) {
  window.addEventListener(SESSION_EVENT, listener);
  return () => window.removeEventListener(SESSION_EVENT, listener);
}

export function hasAdminRole(session) {
  return session?.role === "ADMIN";
}

export function listRegisteredUsers() {
  return ensureUsers().map(toPublicUser);
}

export function updateStoredUser(userId, patch) {
  const users = ensureUsers();
  const index = users.findIndex((item) => item.id === userId);
  if (index < 0) {
    throw new Error("未找到对应用户。");
  }

  const nextUser = {
    ...users[index],
    ...patch,
    displayName: patch.username || users[index].displayName,
  };
  const nextUsers = [...users];
  nextUsers[index] = nextUser;
  saveUsers(nextUsers);
  syncCurrentSession(nextUser);
  return toPublicUser(nextUser);
}

export async function loginUser({ account, password }) {
  await wait();

  if (!account || !password) {
    throw new Error("请输入用户名和密码。");
  }

  const normalizedAccount = String(account).trim().toLowerCase();
  const users = ensureUsers();
  const user = users.find((item) => item.username.toLowerCase() === normalizedAccount);

  if (!user || user.password !== password) {
    throw new Error("用户名或密码错误。");
  }
  if (user.status !== USER_STATUS_ACTIVE) {
    throw new Error("该账号已被封禁，无法登录。");
  }

  const session = toSession(user);
  writeSession(session);
  return session;
}

export async function registerUser(form) {
  await wait();

  const username = String(form.username || "").trim();
  const phone = String(form.phone || "").trim();
  const email = String(form.email || "").trim().toLowerCase();
  const password = String(form.password || "");
  const confirmPassword = String(form.confirmPassword || "");

  if (!username || !phone || !email || !password) {
    throw new Error("请完整填写注册信息。");
  }
  if (!/^1\d{10}$/.test(phone)) {
    throw new Error("请输入正确的 11 位手机号。");
  }
  if (password.length < 6) {
    throw new Error("密码长度至少为 6 位。");
  }
  if (password !== confirmPassword) {
    throw new Error("两次输入的密码不一致。");
  }

  const users = ensureUsers();
  if (users.some((item) => item.username.toLowerCase() === username.toLowerCase())) {
    throw new Error("用户名已存在。");
  }
  if (users.some((item) => item.phone === phone)) {
    throw new Error("手机号已存在。");
  }
  if (users.some((item) => item.email.toLowerCase() === email)) {
    throw new Error("邮箱已存在。");
  }

  const user = {
    id: `USR-${Date.now()}`,
    username,
    phone,
    email,
    password,
    displayName: username,
    role: "USER",
    status: USER_STATUS_ACTIVE,
    origin: "注册账号",
  };

  saveUsers([...users, user]);

  const session = toSession(user);
  writeSession(session);
  return session;
}

export function logoutUser() {
  window.localStorage.removeItem(SESSION_KEY);
  notifySessionChanged();
}
