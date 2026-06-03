<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  createVehicle,
  deleteVehicle,
  getAdminConsoleData,
  getAlertLevelOptions,
  getAlertStatusOptions,
  getUserRoleOptions,
  getUserStatusOptions,
  getVehicleStatusOptions,
  updateAlertManagement,
  updateUserManagement,
  updateVehicle,
} from "../services/adminService";

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

defineEmits(["open-dashboard"]);

const loading = ref(true);
const data = ref(null);
const activeSection = ref("overview");
const feedback = reactive({
  type: "",
  text: "",
});

const menuItems = [
  { id: "overview", label: "概览", hint: "整体状态" },
  { id: "users", label: "用户管理", hint: "角色与账号状态" },
  { id: "vehicles", label: "车辆管理", hint: "新增、编辑、删除" },
  { id: "alerts", label: "告警处理", hint: "指派与处理状态" },
];

const userRoleOptions = getUserRoleOptions();
const userStatusOptions = getUserStatusOptions();
const vehicleStatusOptions = getVehicleStatusOptions();
const alertLevelOptions = getAlertLevelOptions();
const alertStatusOptions = getAlertStatusOptions();

const userDrafts = reactive({});
const alertDrafts = reactive({});
const vehicleForm = reactive({
  vehicleId: "",
  cargoName: "",
  status: vehicleStatusOptions[0],
  driver: "",
  route: "",
});
const editingVehicleId = ref("");

const overviewCards = computed(() => data.value?.overviewCards || []);
const users = computed(() => data.value?.users || []);
const vehicles = computed(() => data.value?.vehicles || []);
const alerts = computed(() => data.value?.alerts || []);

function setFeedback(type, text) {
  feedback.type = type;
  feedback.text = text;
}

function resetVehicleForm() {
  vehicleForm.vehicleId = "";
  vehicleForm.cargoName = "";
  vehicleForm.status = vehicleStatusOptions[0];
  vehicleForm.driver = "";
  vehicleForm.route = "";
  editingVehicleId.value = "";
}

function ensureUserDraft(user) {
  if (!userDrafts[user.id]) {
    userDrafts[user.id] = {
      role: user.role,
      status: user.status,
    };
  }
  return userDrafts[user.id];
}

function ensureAlertDraft(alert) {
  if (!alertDrafts[alert.id]) {
    alertDrafts[alert.id] = {
      owner: alert.owner,
      status: alert.status,
      level: alert.level,
      note: alert.note || "",
    };
  }
  return alertDrafts[alert.id];
}

function syncUserDrafts(nextUsers) {
  Object.keys(userDrafts).forEach((key) => {
    delete userDrafts[key];
  });
  nextUsers.forEach((user) => {
    userDrafts[user.id] = {
      role: user.role,
      status: user.status,
    };
  });
}

function syncAlertDrafts(nextAlerts) {
  Object.keys(alertDrafts).forEach((key) => {
    delete alertDrafts[key];
  });
  nextAlerts.forEach((alert) => {
    alertDrafts[alert.id] = {
      owner: alert.owner,
      status: alert.status,
      level: alert.level,
      note: alert.note || "",
    };
  });
}

async function loadConsole() {
  loading.value = true;
  try {
    const nextData = await getAdminConsoleData(props.currentUser);
    data.value = nextData;
    syncUserDrafts(nextData.users || []);
    syncAlertDrafts(nextData.alerts || []);
  } catch (error) {
    setFeedback("error", error.message || "后台数据加载失败，请稍后重试。");
  } finally {
    loading.value = false;
  }
}

async function saveUser(user) {
  const draft = ensureUserDraft(user);
  try {
    await updateUserManagement(user.id, {
      role: draft.role,
      status: draft.status,
    });
    await loadConsole();
    setFeedback("success", `已更新用户 ${user.username} 的角色和状态。`);
  } catch (error) {
    setFeedback("error", error.message);
  }
}

function editVehicle(vehicle) {
  editingVehicleId.value = vehicle.vehicleId;
  vehicleForm.vehicleId = vehicle.vehicleId;
  vehicleForm.cargoName = vehicle.cargoName;
  vehicleForm.status = vehicle.status;
  vehicleForm.driver = vehicle.driver;
  vehicleForm.route = vehicle.route;
  activeSection.value = "vehicles";
}

async function submitVehicle() {
  try {
    if (editingVehicleId.value) {
      await updateVehicle(editingVehicleId.value, vehicleForm);
      setFeedback("success", `已更新车辆 ${vehicleForm.vehicleId}。`);
    } else {
      await createVehicle(vehicleForm);
      setFeedback("success", `已新增车辆 ${vehicleForm.vehicleId}。`);
    }
    resetVehicleForm();
    await loadConsole();
  } catch (error) {
    setFeedback("error", error.message);
  }
}

async function removeVehicle(vehicleId) {
  const confirmed = window.confirm(`确认删除车辆 ${vehicleId} 吗？`);
  if (!confirmed) {
    return;
  }

  try {
    await deleteVehicle(vehicleId);
    if (editingVehicleId.value === vehicleId) {
      resetVehicleForm();
    }
    await loadConsole();
    setFeedback("success", `已删除车辆 ${vehicleId}。`);
  } catch (error) {
    setFeedback("error", error.message);
  }
}

async function saveAlert(alert) {
  const draft = ensureAlertDraft(alert);
  try {
    await updateAlertManagement(alert.id, draft);
    await loadConsole();
    setFeedback("success", `已更新告警 ${alert.id} 的处理信息。`);
  } catch (error) {
    setFeedback("error", error.message);
  }
}

async function markAlertHandled(alert) {
  const draft = ensureAlertDraft(alert);
  draft.status = "已处理";
  await saveAlert(alert);
}

onMounted(() => {
  loadConsole();
});
</script>

<template>
  <section class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <h2>后台管理中心</h2>
      </div>

      <nav class="admin-nav" aria-label="管理员导航">
        <button
          v-for="item in menuItems"
          :key="item.id"
          :class="['admin-nav-item', { active: activeSection === item.id }]"
          type="button"
          @click="activeSection = item.id"
        >
          <strong>{{ item.label }}</strong>
          <span>{{ item.hint }}</span>
        </button>
      </nav>

      <div class="admin-sidecard">
        <span>当前管理员</span>
        <strong>{{ currentUser.displayName }}</strong>
        <small>{{ currentUser.roleLabel }}</small>
      </div>
    </aside>

    <main class="admin-content">
      <header class="admin-content-head">
        <div>
          <h1>管理员后台管理</h1>
        </div>
        <button class="ghost-button" type="button" :disabled="loading" @click="loadConsole">
          {{ loading ? "刷新中..." : "刷新数据" }}
        </button>
      </header>

      <p v-if="feedback.text" :class="['admin-feedback', feedback.type]">{{ feedback.text }}</p>

      <section v-if="activeSection === 'overview'" class="admin-section">
        <div class="admin-card-grid">
          <article v-for="card in overviewCards" :key="card.label" class="admin-metric">
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
            <small>{{ card.detail }}</small>
          </article>
        </div>
      </section>

      <section v-else-if="activeSection === 'users'" class="admin-section">
        <section class="admin-card">
          <div class="panel-head compact">
            <div>
              <h3>用户管理</h3>
            </div>
          </div>
          <div class="table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>用户名</th>
                  <th>手机号</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>来源</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td>{{ user.username }}</td>
                  <td>{{ user.phone }}</td>
                  <td>
                    <select v-model="ensureUserDraft(user).role" class="admin-select">
                      <option v-for="item in userRoleOptions" :key="item.value" :value="item.value">
                        {{ item.label }}
                      </option>
                    </select>
                  </td>
                  <td>
                    <select v-model="ensureUserDraft(user).status" class="admin-select">
                      <option v-for="item in userStatusOptions" :key="item.value" :value="item.value">
                        {{ item.label }}
                      </option>
                    </select>
                  </td>
                  <td>{{ user.origin }}</td>
                  <td>
                    <button class="table-action" type="button" @click="saveUser(user)">保存</button>
                  </td>
                </tr>
                <tr v-if="!users.length">
                  <td colspan="6">暂无用户数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'vehicles'" class="admin-section admin-stack">
        <section class="admin-card">
          <div class="panel-head compact">
            <div>
              <h3>{{ editingVehicleId ? "编辑车辆" : "新增车辆" }}</h3>
            </div>
          </div>

          <div class="admin-form-grid">
            <label class="admin-field">
              <span>车辆编号</span>
              <input v-model.trim="vehicleForm.vehicleId" class="admin-input" type="text" placeholder="例如 CC-VA-08" />
            </label>
            <label class="admin-field">
              <span>货物类型</span>
              <input v-model.trim="vehicleForm.cargoName" class="admin-input" type="text" placeholder="例如 疫苗" />
            </label>
            <label class="admin-field">
              <span>车辆状态</span>
              <select v-model="vehicleForm.status" class="admin-select">
                <option v-for="status in vehicleStatusOptions" :key="status" :value="status">{{ status }}</option>
              </select>
            </label>
            <label class="admin-field">
              <span>司机</span>
              <input v-model.trim="vehicleForm.driver" class="admin-input" type="text" placeholder="请输入司机姓名" />
            </label>
            <label class="admin-field admin-field-wide">
              <span>路线</span>
              <input v-model.trim="vehicleForm.route" class="admin-input" type="text" placeholder="例如 北京仓库 -> 区域医院" />
            </label>
          </div>

          <div class="admin-inline-actions">
            <button class="ghost-button primary" type="button" @click="submitVehicle">
              {{ editingVehicleId ? "保存修改" : "新增车辆" }}
            </button>
            <button v-if="editingVehicleId" class="ghost-button" type="button" @click="resetVehicleForm">取消编辑</button>
          </div>
        </section>

        <section class="admin-card">
          <div class="panel-head compact">
            <div>
              <h3>车辆列表</h3>
            </div>
          </div>
          <div class="table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>车辆编号</th>
                  <th>货物类型</th>
                  <th>状态</th>
                  <th>司机</th>
                  <th>路线</th>
                  <th>最近更新</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="vehicle in vehicles" :key="vehicle.vehicleId">
                  <td>{{ vehicle.vehicleId }}</td>
                  <td>{{ vehicle.cargoName }}</td>
                  <td>{{ vehicle.status }}</td>
                  <td>{{ vehicle.driver }}</td>
                  <td>{{ vehicle.route }}</td>
                  <td>{{ vehicle.updatedAt }}</td>
                  <td class="admin-actions-cell">
                    <button class="table-action" type="button" @click="editVehicle(vehicle)">编辑</button>
                    <button class="table-action danger" type="button" @click="removeVehicle(vehicle.vehicleId)">删除</button>
                  </td>
                </tr>
                <tr v-if="!vehicles.length">
                  <td colspan="7">暂无车辆数据，可先新增车辆。</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'alerts'" class="admin-section">
        <div v-if="alerts.length" class="admin-alerts-grid">
          <article v-for="alert in alerts" :key="alert.id" :class="['admin-alert-card', alert.level.toLowerCase()]">
            <div class="admin-alert-head">
              <div>
                <strong>{{ alert.title }}</strong>
                <small>{{ alert.vehicleId }}</small>
              </div>
              <span class="alert-level">{{ alert.levelLabel }}</span>
            </div>

            <p>{{ alert.detail }}</p>

            <div class="admin-form-grid compact">
              <label class="admin-field">
                <span>处理人</span>
                <input v-model.trim="ensureAlertDraft(alert).owner" class="admin-input" type="text" />
              </label>
              <label class="admin-field">
                <span>优先级</span>
                <select v-model="ensureAlertDraft(alert).level" class="admin-select">
                  <option v-for="item in alertLevelOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label class="admin-field">
                <span>处理状态</span>
                <select v-model="ensureAlertDraft(alert).status" class="admin-select">
                  <option v-for="item in alertStatusOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label class="admin-field admin-field-wide">
                <span>处理备注</span>
                <textarea v-model.trim="ensureAlertDraft(alert).note" class="admin-textarea" rows="3"></textarea>
              </label>
            </div>

            <div class="admin-inline-actions">
              <button class="table-action" type="button" @click="saveAlert(alert)">保存处理结果</button>
              <button class="table-action success" type="button" @click="markAlertHandled(alert)">标记为已处理</button>
            </div>
          </article>
        </div>
        <section v-else class="admin-card">
          <p>当前没有待处理告警。</p>
        </section>
      </section>
    </main>
  </section>
</template>
