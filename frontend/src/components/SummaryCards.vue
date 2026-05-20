<script setup>
defineProps({
  cards: {
    type: Array,
    required: true,
  },
});
</script>

<template>
  <section class="summary-grid">
    <template v-if="!cards.length">
      <article class="empty-card">
        <h3>等待接口数据</h3>
        <p>连接成功后，这里会展示车辆状态卡片、风险等级和冷链运行指标。</p>
      </article>
    </template>

    <template v-else>
      <article v-for="card in cards" :key="card.label" class="summary-card" :class="card.tone || ''">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.extra }}</small>
      </article>
    </template>
  </section>
</template>
