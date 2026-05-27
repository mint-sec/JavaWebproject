<script setup>
import { computed, onBeforeUnmount, onMounted } from "vue";
import SummaryCards from "./components/SummaryCards.vue";
import RouteMapPanel from "./components/RouteMapPanel.vue";
import TemperaturePanel from "./components/TemperaturePanel.vue";
import AlertsPanel from "./components/AlertsPanel.vue";
import { useDashboard } from "./composables/useDashboard";

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
        <p class="eyebrow">Vue 3 + Vite Frontend</p>
        <h1>冷链运输温控预警平台</h1>
        <p class="topbar-desc">前端已重构为 Vue 3 + Vite，支持实时轮询、风险分级高亮、趋势联动与轨迹回放。</p>
      </div>

      <div class="topbar-meta">
        <div class="meta-card">
          <span>监控场景</span>
          <strong>{{ dashboard.sceneLabel.value }}</strong>
          <small>单车监控大屏</small>
        </div>

        <div class="meta-card meta-select">
          <span>当前车辆</span>
          <label class="select-wrap" for="vehicle-select">
            <select
              id="vehicle-select"
              aria-label="选择车辆"
              :value="dashboard.state.vehicleId"
              @change="dashboard.selectVehicle($event.target.value)"
            >
              <option v-if="!dashboard.state.vehicles.length" value="">等待载入</option>
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
          <span>接口状态</span>
          <strong>{{ dashboard.syncStatus.value }}</strong>
          <small>{{ dashboard.syncDetail.value }}</small>
        </div>
      </div>
    </header>

    <section class="status-strip">
      <div>
        <span class="status-label">同步状态</span>
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
