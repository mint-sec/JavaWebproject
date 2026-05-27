<script setup>
defineProps({
  alerts: { type: Array, required: true },
  alertCountTone: { type: String, default: "" },
  levelLabel: { type: Function, required: true },
});
</script>

<template>
  <aside class="panel panel-alerts">
    <div class="panel-head">
      <div>
        <p class="panel-kicker">告警信息面板</p>
        <h2>实时告警与高亮处置建议</h2>
      </div>
      <span class="pill" :class="alertCountTone">{{ alerts.length }} 条告警</span>
    </div>

    <div class="alerts-list">
      <template v-if="!alerts.length">
        <article class="empty-card">
          <h3>暂无告警</h3>
          <p>当前车辆未返回告警记录，页面会继续轮询并自动刷新高亮状态。</p>
        </article>
      </template>

      <template v-else>
        <article
          v-for="(alert, index) in alerts"
          :key="alert.alertId || `${alert.title}-${index}`"
          class="alert-card"
          :class="[alert.level.toLowerCase(), index === 0 ? 'highlight' : '']"
        >
          <header>
            <h3>{{ alert.title }}</h3>
            <span class="alert-tag" :class="alert.level.toLowerCase()">{{ levelLabel(alert.level) }}</span>
          </header>
          <p>{{ alert.detail }}</p>
          <footer>
            <span class="alert-index">{{ String(index + 1).padStart(2, "0") }}</span>
            <span>{{ alert.suggestion }}</span>
          </footer>
        </article>
      </template>
    </div>
  </aside>
</template>
