import { clearSession, readSession } from "./sessionStore";
import { emitAppNotice } from "./noticeCenter";

const API_BASE_CANDIDATES = (() => {
  if (window.location.protocol === "file:") {
    return ["http://localhost:18081/api/v1"];
  }

  if (["63342", "5500"].includes(window.location.port)) {
    return ["http://localhost:18081/api/v1", "/api/v1"];
  }

  return ["/api/v1", "http://localhost:18081/api/v1"];
})();

let resolvedApiBase = "";

function shouldTryNextBase(error) {
  return error instanceof TypeError || String(error.message || "").includes("Failed to fetch") || Boolean(error?.retryable);
}

function shouldBroadcastError(error) {
  const statusCode = Number(error?.statusCode || 0);
  return statusCode === 401
    || statusCode === 403
    || statusCode >= 500
    || error instanceof TypeError
    || String(error?.message || "").includes("Failed to fetch");
}

export async function apiRequest(path, { method = "GET", body, withAuth = true } = {}) {
  const candidates = resolvedApiBase ? [resolvedApiBase] : API_BASE_CANDIDATES;
  let lastError = null;

  for (const base of candidates) {
    try {
      const headers = {
        Accept: "application/json",
      };

      if (body !== undefined) {
        headers["Content-Type"] = "application/json";
      }

      if (withAuth) {
        const session = readSession();
        if (session?.token) {
          headers.Authorization = `Bearer ${session.token}`;
        }
      }

      const response = await fetch(`${base}${path}`, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        cache: "no-store",
      });

      const payload = await response.json().catch(() => ({
        success: false,
        message: `请求失败：${response.status}`,
      }));

      if (!response.ok || !payload.success) {
        const error = new Error(payload.message || `请求失败：${response.status}`);
        error.statusCode = response.status;
        error.retryable = response.status === 404 || response.status >= 500;
        if (withAuth && response.status === 401) {
          resolvedApiBase = "";
          clearSession();
          error.message = payload.message || "登录状态已失效，请重新登录。";
        }
        throw error;
      }

      resolvedApiBase = base;
      return payload.data;
    } catch (error) {
      lastError = error;
      if (!shouldTryNextBase(error)) {
        break;
      }
    }
  }

  const finalError = lastError || new Error("接口请求失败，请稍后重试。");

  if (shouldBroadcastError(finalError)) {
    emitAppNotice({
      type: Number(finalError.statusCode) === 403 ? "warning" : "error",
      message: finalError.message || "接口请求失败，请稍后重试。",
    });
  }

  throw finalError;
}

export function resetApiBaseCache() {
  resolvedApiBase = "";
}

export function getResolvedApiBase() {
  return resolvedApiBase;
}
