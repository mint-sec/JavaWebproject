import { apiRequest, resetApiBaseCache } from "./apiClient";
import { clearSession, onSessionChanged, readSession, writeSession } from "./sessionStore";

export const USER_STATUS_ACTIVE = "启用中";
export const USER_STATUS_BANNED = "已封禁";

function isValidPhone(phone) {
  return /^1\d{10}$/.test(phone);
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export function getCurrentSession() {
  return readSession();
}

export function hasAdminRole(session) {
  return session?.role === "ADMIN";
}

export async function loginUser({ account, password }) {
  const normalizedAccount = String(account || "").trim();
  const normalizedPassword = String(password || "");

  if (!normalizedAccount) {
    throw new Error("请输入用户名。");
  }
  if (!normalizedPassword) {
    throw new Error("请输入密码。");
  }

  resetApiBaseCache();
  const session = await apiRequest("/auth/login", {
    method: "POST",
    body: {
      account: normalizedAccount,
      password: normalizedPassword,
    },
    withAuth: false,
  });

  writeSession(session);
  return session;
}

export async function registerUser(form) {
  const payload = {
    username: String(form.username || "").trim(),
    phone: String(form.phone || "").trim(),
    email: String(form.email || "").trim().toLowerCase(),
    password: String(form.password || ""),
    confirmPassword: String(form.confirmPassword || ""),
  };

  if (!payload.username) {
    throw new Error("请输入用户名。用户名将同时作为登录账号和页面显示名称。");
  }
  if (payload.username.length < 3) {
    throw new Error("用户名至少需要 3 个字符。");
  }
  if (payload.username.length > 20) {
    throw new Error("用户名不能超过 20 个字符。");
  }
  if (!payload.phone) {
    throw new Error("请输入手机号。");
  }
  if (!isValidPhone(payload.phone)) {
    throw new Error("手机号格式不正确，请输入 11 位大陆手机号。");
  }
  if (!payload.email) {
    throw new Error("请输入邮箱。");
  }
  if (!isValidEmail(payload.email)) {
    throw new Error("邮箱格式不正确，请重新输入。");
  }
  if (!payload.password) {
    throw new Error("请输入密码。");
  }
  if (payload.password.length < 6) {
    throw new Error("密码至少需要 6 位。");
  }
  if (payload.password.length > 32) {
    throw new Error("密码不能超过 32 位。");
  }
  if (!payload.confirmPassword) {
    throw new Error("请再次输入确认密码。");
  }
  if (payload.password !== payload.confirmPassword) {
    throw new Error("两次输入的密码不一致。");
  }

  resetApiBaseCache();
  const session = await apiRequest("/auth/register", {
    method: "POST",
    body: payload,
    withAuth: false,
  });

  writeSession(session);
  return session;
}

export function logoutUser() {
  const session = readSession();

  if (session?.token) {
    apiRequest("/auth/logout", {
      method: "POST",
      withAuth: true,
    }).catch(() => {});
  }

  clearSession();
}

export { onSessionChanged };
