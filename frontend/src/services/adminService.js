import { apiRequest } from "./apiClient";
import { USER_STATUS_ACTIVE, USER_STATUS_BANNED } from "./authService";

const VEHICLE_STATUS_OPTIONS = ["运输中", "待命中", "维修中", "已停用"];
const ALERT_LEVEL_OPTIONS = ["HIGH", "MEDIUM", "LOW"];
const ALERT_STATUS_OPTIONS = ["待处理", "处理中", "已处理"];

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

export async function getAdminConsoleData() {
  return apiRequest("/admin/console");
}

export async function getBusinessWorkspaceData() {
  return apiRequest("/workspace/console");
}

export async function updateUserManagement(userId, payload) {
  return apiRequest(`/admin/users/${encodeURIComponent(userId)}`, {
    method: "PATCH",
    body: payload,
  });
}

export async function createVehicleForUser(_currentUser, payload) {
  return apiRequest("/workspace/vehicles", {
    method: "POST",
    body: payload,
  });
}

export async function updateVehicleForUser(_currentUser, vehicleKey, payload) {
  return apiRequest(`/workspace/vehicles/${encodeURIComponent(vehicleKey)}`, {
    method: "PUT",
    body: payload,
  });
}

export async function deleteVehicleForUser(_currentUser, vehicleKey) {
  return apiRequest(`/workspace/vehicles/${encodeURIComponent(vehicleKey)}`, {
    method: "DELETE",
  });
}

export async function updateAlertForUser(_currentUser, alertId, payload) {
  return apiRequest(`/workspace/alerts/${encodeURIComponent(alertId)}`, {
    method: "PATCH",
    body: payload,
  });
}
