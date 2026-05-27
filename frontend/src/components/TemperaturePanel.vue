<script setup>
import { computed } from "vue";
import { formatNumber } from "../composables/dashboardUtils";

const props = defineProps({
  history: { type: Array, required: true },
  activePoint: { type: Object, default: null },
  activeIndex: { type: Number, required: true },
  latestTelemetry: { type: Object, default: null },
  selectedVehicle: { type: Object, default: null },
  temperatureTone: { type: String, default: "" },
});

const emit = defineEmits(["focus-latest", "set-active-index"]);

const chartData = computed(() => {
  if (!props.history.length || !props.activePoint || !props.latestTelemetry) {
    return null;
  }

  const width = 720;
  const height = 360;
  const padding = { top: 24, right: 24, bottom: 54, left: 52 };
  const innerWidth = width - padding.left - padding.right;
  const innerHeight = height - padding.top - padding.bottom;
  const safeMin = props.selectedVehicle?.safeTempMin ?? 2;
  const safeMax = props.selectedVehicle?.safeTempMax ?? 8;
  const values = props.history.map((item) => item.temperature);
  const min = Math.max(0, Math.floor(Math.min(...values, safeMin) - 1));
  const max = Math.ceil(Math.max(...values, safeMax) + 1);
  const xStep = innerWidth / Math.max(props.history.length - 1, 1);
  const toY = (value) => padding.top + innerHeight - ((value - min) / (max - min || 1)) * innerHeight;
  const points = props.history.map((item, index) => ({
    ...item,
    x: padding.left + index * xStep,
    y: toY(item.temperature),
    label: item.sampleTime.slice(3),
  }));

  return {
    width,
    height,
    padding,
    points,
    polyline: points.map((point) => `${point.x},${point.y}`).join(" "),
    area: `${padding.left},${padding.top + innerHeight} ${points.map((point) => `${point.x},${point.y}`).join(" ")} ${padding.left + innerWidth},${padding.top + innerHeight}`,
    safeTop: toY(safeMax),
    safeHeight: toY(safeMin) - toY(safeMax),
    yTicks: Array.from({ length: 6 }, (_, index) => {
      const value = min + ((max - min) / 5) * index;
      return {
        value: `${value.toFixed(0)}°C`,
        y: toY(value),
      };
    }),
  };
});
</script>

<template>
  <section class="panel panel-chart">
    <div class="panel-head">
      <div>
        <p class="panel-kicker">温度趋势区域</p>
        <h2>最近采样温度趋势联动</h2>
      </div>
      <span class="pill" :class="temperatureTone">{{ activePoint?.trend || "等待数据" }}</span>
    </div>

    <div class="chart-stage">
      <svg v-if="chartData" :viewBox="`0 0 ${chartData.width} ${chartData.height}`" aria-label="温度趋势图">
        <defs>
          <linearGradient id="tempArea" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="rgba(97,216,255,0.32)" />
            <stop offset="100%" stop-color="rgba(97,216,255,0.02)" />
          </linearGradient>
        </defs>

        <rect :x="0" :y="0" :width="chartData.width" :height="chartData.height" fill="transparent" />
        <rect
          :x="chartData.padding.left"
          :y="chartData.safeTop"
          :width="chartData.width - chartData.padding.left - chartData.padding.right"
          :height="chartData.safeHeight"
          fill="rgba(36,215,173,0.08)"
        />

        <g v-for="tick in chartData.yTicks" :key="tick.value">
          <line
            :x1="chartData.padding.left"
            :y1="tick.y"
            :x2="chartData.width - chartData.padding.right"
            :y2="tick.y"
            stroke="rgba(255,255,255,0.08)"
          />
          <text x="8" :y="tick.y + 5" fill="#88a5b0" font-size="12">{{ tick.value }}</text>
        </g>

        <polygon :points="chartData.area" fill="url(#tempArea)" />
        <polyline :points="chartData.polyline" fill="none" stroke="#61d8ff" stroke-width="4" stroke-linejoin="round" stroke-linecap="round" />

        <g
          v-for="(point, index) in chartData.points"
          :key="`${point.sampleTime}-${index}`"
          class="chart-point"
          @click="emit('set-active-index', index)"
        >
          <circle
            :cx="point.x"
            :cy="point.y"
            :r="index === activeIndex ? 7 : 5"
            :fill="index === activeIndex ? '#24d7ad' : '#07151d'"
            :stroke="index === activeIndex ? '#24d7ad' : '#61d8ff'"
            stroke-width="3"
          />
          <text :x="point.x - 18" :y="chartData.height - 18" :fill="index === activeIndex ? '#eff7fa' : '#88a5b0'" font-size="12">
            {{ point.label }}
          </text>
        </g>

        <line
          :x1="chartData.padding.left"
          :y1="chartData.height - chartData.padding.bottom"
          :x2="chartData.width - chartData.padding.right"
          :y2="chartData.height - chartData.padding.bottom"
          stroke="rgba(255,255,255,0.12)"
        />
      </svg>

      <svg v-else viewBox="0 0 720 360" aria-label="温度趋势图">
        <rect x="0" y="0" width="720" height="360" fill="transparent" />
        <text x="360" y="180" text-anchor="middle" fill="#88a5b0" font-size="18">等待温度采样</text>
      </svg>
    </div>

    <div class="focus-card">
      <div>
        <span>联动详情</span>
        <strong>{{ activePoint ? `${activePoint.sampleTime} · ${activePoint.temperature.toFixed(1)}°C` : "等待采样数据" }}</strong>
        <small>
          {{
            activePoint
              ? `湿度 ${formatNumber(activePoint.humidity)}% · 速度 ${formatNumber(activePoint.speed)} km/h · ${activePoint.doorOpen ? "车门开启中" : "车门已关闭"} · 后端记录 ${activePoint.recordTime}`
              : "点击折线中的采样点，可联动查看该时刻的车辆位置和运行指标。"
          }}
        </small>
      </div>
      <button class="ghost-button" type="button" :disabled="!history.length" @click="emit('focus-latest')">回到最新</button>
    </div>

    <div class="chart-footer">
      <div>
        <span>安全温区</span>
        <strong>{{ selectedVehicle ? `${formatNumber(selectedVehicle.safeTempMin)}°C - ${formatNumber(selectedVehicle.safeTempMax)}°C` : "--" }}</strong>
      </div>
      <div>
        <span>当前温度</span>
        <strong>{{ latestTelemetry ? `${latestTelemetry.temperature.toFixed(1)}°C` : "--.-°C" }}</strong>
      </div>
      <div>
        <span>温度趋势</span>
        <strong>{{ activePoint?.trend || "--" }}</strong>
      </div>
    </div>
  </section>
</template>
