<script setup>
import { computed, onBeforeUnmount, onMounted } from "vue";
import SummaryCards from "./components/SummaryCards.vue";
import RouteMapPanel from "./components/RouteMapPanel.vue";
import TemperaturePanel from "./components/TemperaturePanel.vue";
import AlertsPanel from "./components/AlertsPanel.vue";
import { useDashboard } from "./composables/useDashboard";

const dashboard = useDashboard();

const alertCountTone = computed(() => dashboard.toneClass(dashboard.highestLevel.value));
const demoBadgeTone = computed(() => (dashboard.state.demoMode ? "warn" : ""));
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
        <p class="topbar-desc">前端已重构为 Vue 3 + Vite，支持实时轮询、风险分级高亮、趋势联动与轨迹回放，适合录屏演示。</p>
        <div class="hero-actions">
          <button class="ghost-button primary" type="button" @click="dashboard.toggleDemoMode()">
            {{ dashboard.state.demoMode ? "停止演示" : "自动演示" }}
          </button>
          <span class="pill" :class="demoBadgeTone">{{ dashboard.state.demoMessage }}</span>
        </div>
      </div>

      <div class="topbar-meta">
        <div class="meta-card">
          <span>监控场景</span>
          <strong>{{ dashboard.sceneLabel.value }}</strong>
          <small>单车监控大屏 · {{ dashboard.demoSteps[dashboard.state.demoStage]?.title }}</small>
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

    <section class="panel demo-flow">
      <div class="panel-head">
        <div>
          <p class="panel-kicker">Demo Flow</p>
          <h2>录屏与现场演示流程</h2>
        </div>
        <span class="pill" :class="demoBadgeTone">{{ dashboard.state.demoMode ? "自动轮播中" : "手动讲解模式" }}</span>
      </div>

      <div class="demo-step-list">
        <article
          v-for="(step, index) in dashboard.demoSteps"
          :key="step.title"
          class="demo-step"
          :class="{ active: index === dashboard.state.demoStage }"
        >
          <span class="demo-step-index">0{{ index + 1 }}</span>
          <strong>{{ step.title }}</strong>
          <small>{{ step.description }}</small>
        </article>
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
