<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  getAdminConsoleData,
  getUserRoleOptions,
  getUserStatusOptions,
  updateUserManagement,
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
const activeLogTab = ref("login");
const savingUserIds = reactive({});

const menuItems = [
  { id: "overview", label: "平台概览", hint: "平台账号与事项总览" },
  { id: "users", label: "用户管理", hint: "角色与账号状态" },
  { id: "logs", label: "日志中心", hint: "登录日志与操作审计" },
  { id: "services", label: "服务监控", hint: "前后端与算法链路状态" },
];

const userRoleOptions = getUserRoleOptions();
const userStatusOptions = getUserStatusOptions();

const userDrafts = reactive({});

const overviewCards = computed(() => data.value?.overviewCards || []);
const logCenterCards = computed(() => data.value?.logCenterCards || []);
const users = computed(() => data.value?.users || []);
const loginLogs = computed(() => data.value?.loginLogs || []);
const operationLogs = computed(() => data.value?.operationLogs || []);
const serviceMonitors = computed(() => data.value?.serviceMonitors || []);

function setFeedback(type, text) {
  feedback.type = type;
  feedback.text = text;
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

async function loadConsole() {
  loading.value = true;
  try {
    const nextData = await getAdminConsoleData(props.currentUser);
    data.value = nextData;
    syncUserDrafts(nextData.users || []);
  } catch (error) {
    setFeedback("error", error.message || "后台数据加载失败，请稍后重试。");
  } finally {
    loading.value = false;
  }
}

async function saveUser(user) {
  const draft = ensureUserDraft(user);
  if (savingUserIds[user.id]) {
    return;
  }
  savingUserIds[user.id] = true;
  try {
    await updateUserManagement(user.id, {
      role: draft.role,
      status: draft.status,
    }, props.currentUser);
    await loadConsole();
    setFeedback("success", `已更新用户 ${user.username} 的角色和状态。`);
  } catch (error) {
    setFeedback("error", error.message);
  } finally {
    delete savingUserIds[user.id];
  }
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
        <section class="admin-card admin-note">
          <h3>职责调整说明</h3>
          <p>车辆维护和告警处理已迁移到普通用户工作台，管理员后台现在只负责平台账号、角色权限与平台侧事项管理。</p>
        </section>
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
                    <button class="table-action" type="button" :disabled="Boolean(savingUserIds[user.id])" @click="saveUser(user)">
                      {{ savingUserIds[user.id] ? "保存中..." : "保存" }}
                    </button>
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

      <section v-else-if="activeSection === 'logs'" class="admin-section">
        <div class="admin-card-grid">
          <article v-for="card in logCenterCards" :key="card.label" class="admin-metric">
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
            <small>{{ card.detail }}</small>
          </article>
        </div>

        <section class="admin-card">
          <div class="panel-head compact">
            <div>
              <h3>日志中心</h3>
            </div>
          </div>

          <div class="admin-tabs">
            <button
              :class="['admin-tab', { active: activeLogTab === 'login' }]"
              type="button"
              @click="activeLogTab = 'login'"
            >
              登录日志
            </button>
            <button
              :class="['admin-tab', { active: activeLogTab === 'operation' }]"
              type="button"
              @click="activeLogTab = 'operation'"
            >
              操作日志
            </button>
          </div>

          <div v-if="activeLogTab === 'login'" class="table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>时间</th>
                  <th>账号</th>
                  <th>角色</th>
                  <th>结果</th>
                  <th>来源</th>
                  <th>说明</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in loginLogs" :key="log.id">
                  <td>{{ log.time }}</td>
                  <td>{{ log.account }}</td>
                  <td>{{ log.roleLabel }}</td>
                  <td>{{ log.result }}</td>
                  <td>{{ log.ip }}</td>
                  <td>{{ log.detail }}</td>
                </tr>
                <tr v-if="!loginLogs.length">
                  <td colspan="6">暂无登录日志</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>时间</th>
                  <th>模块</th>
                  <th>动作</th>
                  <th>操作人</th>
                  <th>目标对象</th>
                  <th>结果</th>
                  <th>说明</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in operationLogs" :key="log.id">
                  <td>{{ log.time }}</td>
                  <td>{{ log.module }}</td>
                  <td>{{ log.action }}</td>
                  <td>{{ log.operator }}</td>
                  <td>{{ log.target }}</td>
                  <td>{{ log.result }}</td>
                  <td>{{ log.detail }}</td>
                </tr>
                <tr v-if="!operationLogs.length">
                  <td colspan="7">暂无操作日志</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'services'" class="admin-section">
        <section class="admin-card admin-note">
          <h3>服务监控</h3>
          <p>这里用于集中查看前端、后端、算法和数据库链路的状态。当前为前端预留版，后续可以直接接健康检查接口与链路监控接口。</p>
        </section>

        <div class="service-grid">
          <article v-for="service in serviceMonitors" :key="service.id" class="service-card">
            <div class="service-card-head">
              <div>
                <strong>{{ service.name }}</strong>
                <small>{{ service.source }}</small>
              </div>
              <span :class="['status-pill', service.tone]">{{ service.status }}</span>
            </div>

            <p>{{ service.detail }}</p>

            <div class="service-meta">
              <span>响应耗时</span>
              <strong>{{ service.latency }}</strong>
            </div>
            <div class="service-meta">
              <span>最近检查</span>
              <strong>{{ service.checkedAt }}</strong>
            </div>
          </article>
        </div>
      </section>
    </main>
  </section>
</template>
