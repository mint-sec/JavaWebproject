<script setup>
defineProps({
  title: {
    type: String,
    default: "预留接口契约",
  },
  description: {
    type: String,
    default: "",
  },
  contracts: {
    type: Array,
    default: () => [],
  },
});
</script>

<template>
  <section class="contract-panel">
    <div class="panel-head compact">
      <div>
        <p class="panel-kicker">待后端接入</p>
        <h3>{{ title }}</h3>
      </div>
      <span class="pill">当前为 Mock</span>
    </div>

    <p v-if="description" class="contract-desc">{{ description }}</p>

    <div class="contract-list">
      <article v-for="contract in contracts" :key="`${contract.method}-${contract.path}`" class="contract-item">
        <div class="contract-meta">
          <span class="contract-method">{{ contract.method }}</span>
          <code>{{ contract.path }}</code>
        </div>
        <strong>{{ contract.name }}</strong>
        <p>{{ contract.description }}</p>
        <small>{{ contract.note }}</small>
      </article>
    </div>
  </section>
</template>
