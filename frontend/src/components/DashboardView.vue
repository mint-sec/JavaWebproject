<script setup>
import { computed, onBeforeUnmount, onMounted } from "vue";
import SummaryCards from "./SummaryCards.vue";
import RouteMapPanel from "./RouteMapPanel.vue";
import TemperaturePanel from "./TemperaturePanel.vue";
import AlertsPanel from "./AlertsPanel.vue";
import { useDashboard } from "../composables/useDashboard";

const props = defineProps({
  currentUser: {
    type: Object,
    default: null,
  },
  canAccessAdmin: {
    type: Boolean,
    default: false,
  },
});

defineEmits(["open-admin", "logout"]);

const dashboard = useDashboard();

const alertCountTone = computed(() => dashboard.toneClass(dashboard.highestLevel.value));
const dashboardTone = computed(() => {
  const level = String(dashboard.highestLevel.value || "LOW").toLowerCase();
  if (level === "high") {
    return "risk-high";
  }
  if (level === "medium") {
    return "risk-medium";
  }
  return "risk-low";
});

const headStats = computed(() => [
  {
    label: "当前账号",
    value: props.currentUser?.displayName || "--",
    detail: props.currentUser?.roleLabel || "未登录",
  },
  {
    label: "当前车辆",
    value: dashboard.state.vehicleId || "--",
    detail: dashboard.state.vehicleMeta?.plateNumber || "等待选择车辆",
  },
  {
    label: "风险等级",
    value: dashboard.levelLabel(dashboard.highestLevel.value),
    detail: dashboard.state.latestTelemetry?.trend || "状态更新中",
  },
  {
    label: "剩余路程",
    value: dashboard.state.latestTelemetry ? `${dashboard.state.latestTelemetry.remainingKm.toFixed(1)} km` : "--",
    detail: dashboard.state.latestTelemetry ? `当前速度 ${dashboard.state.latestTelemetry.speed.toFixed(1)} km/h` : "等待实时更新",
  },
]);

onMounted(() => {
  dashboard.initialize();
});

onBeforeUnmount(() => {
  dashboard.cleanup();
});
</script>

<template>
  <div class="dashboard-shell" :class="dashboardTone">
    <header class="topbar">
      <div class="topbar-copy">
        <div class="topbar-intro">
          <h1>冷链运输监控大屏</h1>
          <p class="topbar-brief">聚焦运输过程中的温控、位置轨迹与风险告警，帮助值守人员快速掌握当前车辆状态。</p>
        </div>

        <div class="hero-glance">
          <article v-for="item in headStats" :key="item.label" class="hero-chip">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.detail }}</small>
          </article>
        </div>

        <div class="hero-actions">
          <button v-if="canAccessAdmin" class="ghost-button primary" type="button" @click="$emit('open-admin')">
            进入后台管理
          </button>
          <button class="ghost-button" type="button" @click="$emit('logout')">退出登录</button>
        </div>
      </div>

      <div class="topbar-meta">
        <div class="meta-card">
          <span>场景</span>
          <strong>{{ dashboard.sceneLabel.value }}</strong>
          <small>单车监控</small>
        </div>

        <div class="meta-card meta-select">
          <span>车辆</span>
          <label class="select-wrap" for="vehicle-select">
            <select
              id="vehicle-select"
              aria-label="选择车辆"
              :value="dashboard.state.vehicleId"
              @change="dashboard.selectVehicle($event.target.value)"
            >
              <option v-if="!dashboard.state.vehicles.length" value="">加载中</option>
              <option v-for="vehicle in dashboard.state.vehicles" :key="vehicle.vehicleId" :value="vehicle.vehicleId">
                {{ vehicle.vehicleId }}
              </option>
            </select>
          </label>
          <small>{{ dashboard.vehicleMetaLabel.value }}</small>
        </div>

        <div class="meta-card">
          <span>系统时间</span>
          <strong>{{ dashboard.systemTimeLabel.value }}</strong>
          <small>{{ dashboard.recordTimeLabel.value }}</small>
        </div>

        <div class="meta-card">
          <span>同步状态</span>
          <strong>{{ dashboard.syncStatus.value }}</strong>
          <small>{{ dashboard.syncDetail.value }}</small>
        </div>
      </div>
    </header>

    <section class="status-strip">
      <div>
        <span class="status-label">实时监控</span>
        <strong>{{ dashboard.syncHeadline.value }}</strong>
      </div>
      <div class="status-actions">
        <span class="pill" :class="dashboard.pollingTone.value">{{ dashboard.pollingLabel.value }}</span>
        <button class="ghost-button" type="button" :disabled="dashboard.state.isLoading" @click="dashboard.refreshNow()">
          立即刷新
        </button>
      </div>
    </section>

    <SummaryCards :cards="dashboard.summaryCards.value" />

    <main class="dashboard-grid">
      <RouteMapPanel
        :trajectory="dashboard.state.trajectory"
        :active-point="dashboard.activePoint.value"
        :active-index="dashboard.activeIndex.value"
        :latest-index="dashboard.latestIndex.value"
        :replay-active="dashboard.state.replayActive"
        :route-tone="dashboard.routeTone.value"
        @toggle-replay="dashboard.toggleReplay()"
        @set-active-index="dashboard.setActiveIndex"
      />

      <TemperaturePanel
        :history="dashboard.state.history"
        :active-point="dashboard.activePoint.value"
        :active-index="dashboard.activeIndex.value"
        :latest-telemetry="dashboard.state.latestTelemetry"
        :selected-vehicle="dashboard.state.vehicleMeta"
        :temperature-tone="dashboard.temperatureTone.value"
        @focus-latest="dashboard.focusLatest()"
        @set-active-index="dashboard.setActiveIndex"
      />

      <AlertsPanel :alerts="dashboard.state.alerts" :alert-count-tone="alertCountTone" :level-label="dashboard.levelLabel" />
    </main>
  </div>
</template>
