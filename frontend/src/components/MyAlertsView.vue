<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  getAlertLevelOptions,
  getAlertStatusOptions,
  getBusinessWorkspaceData,
  updateAlertForUser,
} from "../services/adminService";

const props = defineProps({
  currentUser: {
    type: Object,
    required: true,
  },
});

const loading = ref(true);
const data = ref(null);
const feedback = reactive({
  type: "",
  text: "",
});
const alertDrafts = reactive({});

const alertLevelOptions = getAlertLevelOptions();
const alertStatusOptions = getAlertStatusOptions();

const overviewCards = computed(() => {
  const cards = data.value?.overviewCards || [];
  return cards.filter((card) => ["待处理告警", "处理中告警", "已处理告警"].includes(card.label));
});
const alerts = computed(() => data.value?.alerts || []);

function setFeedback(type, text) {
  feedback.type = type;
  feedback.text = text;
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

async function loadAlertsWorkspace() {
  loading.value = true;
  try {
    const nextData = await getBusinessWorkspaceData(props.currentUser);
    data.value = nextData;
    syncAlertDrafts(nextData.alerts || []);
  } catch (error) {
    setFeedback("error", error.message || "告警数据加载失败，请稍后重试。");
  } finally {
    loading.value = false;
  }
}

async function saveAlert(alert) {
  try {
    await updateAlertForUser(props.currentUser, alert.id, ensureAlertDraft(alert));
    await loadAlertsWorkspace();
    setFeedback("success", `已更新告警 ${alert.id} 的处理结果。`);
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
  loadAlertsWorkspace();
});
</script>

<template>
  <section class="workspace-section">
    <div class="workspace-head">
      <div>
        <p class="panel-kicker">业务中心</p>
        <h1>我的告警</h1>
        <p class="topbar-brief">告警处理独立成页，用户可以更专注地跟进分配给自己的异常事项，而不是在监控首页里来回切换。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadAlertsWorkspace">
        {{ loading ? "刷新中..." : "刷新告警数据" }}
      </button>
    </div>

    <p v-if="feedback.text" :class="['admin-feedback', feedback.type]">{{ feedback.text }}</p>

    <div class="workspace-metrics">
      <article v-for="card in overviewCards" :key="card.label" class="admin-metric">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.detail }}</small>
      </article>
    </div>

    <section class="admin-card workspace-card">
      <div class="workspace-card-head">
        <div>
          <h3>告警处理列表</h3>
          <p>只展示当前登录账号负责的业务告警，不包含平台侧审核事项。</p>
        </div>
      </div>

      <div v-if="alerts.length" class="admin-alerts-grid workspace-alerts-grid">
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

      <div v-else class="workspace-empty">
        <h3>当前没有分配给你的业务告警</h3>
        <p>后续接入后端后，这里可以直接只返回当前登录用户负责的告警列表。</p>
      </div>
    </section>
  </section>
</template>
