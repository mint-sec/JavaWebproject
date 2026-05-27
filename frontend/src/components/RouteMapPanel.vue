<script setup>
import { computed } from "vue";
import { formatNumber, mapTrajectoryToCanvas } from "../composables/dashboardUtils";

const props = defineProps({
  trajectory: { type: Array, required: true },
  activePoint: { type: Object, default: null },
  activeIndex: { type: Number, required: true },
  latestIndex: { type: Number, required: true },
  replayActive: { type: Boolean, required: true },
  routeTone: { type: String, default: "" },
});

const emit = defineEmits(["toggle-replay", "set-active-index"]);

const mappedPoints = computed(() => mapTrajectoryToCanvas(props.trajectory));
const routePath = computed(() => mappedPoints.value.map((point) => `${point.x},${point.y}`).join(" "));
const latestPoint = computed(() => mappedPoints.value[props.latestIndex] || null);
const replayLabel = computed(() => (props.latestIndex <= 0 ? "至少需要 2 个采样点才能回放" : `当前第 ${props.activeIndex + 1} / ${props.latestIndex + 1} 个采样点`));
</script>

<template>
  <section class="panel panel-map">
    <div class="panel-head">
      <div>
        <p class="panel-kicker">地图轨迹区域</p>
        <h2>车辆轨迹与位置回放</h2>
      </div>
      <span class="pill" :class="routeTone">{{ replayActive ? `回放中 ${activeIndex + 1}/${Math.max(trajectory.length, 1)}` : trajectory.length ? `已采样 ${trajectory.length} 次` : "等待采样" }}</span>
    </div>

    <div class="map-stage">
      <svg v-if="trajectory.length && activePoint && latestPoint" viewBox="0 0 720 380" aria-label="车辆轨迹视图">
        <defs>
          <linearGradient id="trajectoryGlow" x1="0%" x2="100%">
            <stop offset="0%" stop-color="#61d8ff" />
            <stop offset="100%" stop-color="#24d7ad" />
          </linearGradient>
          <filter id="traceGlow">
            <feGaussianBlur stdDeviation="4" result="blur" />
            <feMerge>
              <feMergeNode in="blur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        <rect x="0" y="0" width="720" height="380" rx="24" fill="transparent" />

        <template v-for="lineIndex in 9" :key="`h-${lineIndex}`">
          <line :x1="0" :y1="(lineIndex - 1) * 48" x2="720" :y2="(lineIndex - 1) * 48" stroke="rgba(255,255,255,0.06)" />
        </template>

        <template v-for="lineIndex in 16" :key="`v-${lineIndex}`">
          <line :x1="(lineIndex - 1) * 48" y1="0" :x2="(lineIndex - 1) * 48" y2="380" stroke="rgba(255,255,255,0.05)" />
        </template>

        <polyline :points="routePath" fill="none" stroke="url(#trajectoryGlow)" stroke-width="8" stroke-linecap="round" stroke-linejoin="round" opacity="0.18" />
        <polyline :points="routePath" fill="none" stroke="url(#trajectoryGlow)" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round" />

        <g v-for="(point, index) in mappedPoints" :key="`${point.sampleTime}-${index}`">
          <circle
            :cx="point.x"
            :cy="point.y"
            :r="index === latestIndex ? 6 : 4"
            :fill="index === activeIndex ? '#ffbf5a' : '#61d8ff'"
            :opacity="index === activeIndex || index === latestIndex ? 1 : 0.72"
          />
        </g>

        <g v-if="latestPoint">
          <circle :cx="latestPoint.x" :cy="latestPoint.y" r="18" fill="rgba(36,215,173,0.2)" />
          <circle :cx="latestPoint.x" :cy="latestPoint.y" r="10" fill="#24d7ad" filter="url(#traceGlow)" />
          <text :x="latestPoint.x + 16" :y="latestPoint.y + 5" fill="#eff7fa" font-size="14">最新位置</text>
        </g>

        <g v-if="mappedPoints[activeIndex]">
          <circle :cx="mappedPoints[activeIndex].x" :cy="mappedPoints[activeIndex].y" r="14" fill="rgba(255,191,90,0.18)" />
          <circle :cx="mappedPoints[activeIndex].x" :cy="mappedPoints[activeIndex].y" r="8" fill="#ffbf5a" />
          <text :x="mappedPoints[activeIndex].x + 16" :y="mappedPoints[activeIndex].y - 12" fill="#ffddb0" font-size="14">
            {{ activePoint.sampleTime }}
          </text>
        </g>

        <text x="24" y="32" fill="#88a5b0" font-size="13">当前视图为经纬度轨迹投影，适用于实时监控与轨迹回放演示。</text>
      </svg>

      <svg v-else viewBox="0 0 720 380" aria-label="车辆轨迹视图">
        <rect x="0" y="0" width="720" height="380" fill="transparent" />
        <text x="360" y="190" text-anchor="middle" fill="#88a5b0" font-size="18">等待轨迹数据</text>
      </svg>

      <div class="map-legend">
        <span><i class="legend-dot trail"></i>轨迹历史</span>
        <span><i class="legend-dot latest"></i>最新采样</span>
        <span><i class="legend-dot replay"></i>回放定位</span>
      </div>
    </div>

    <div class="replay-toolbar">
      <button class="ghost-button" type="button" :disabled="latestIndex <= 0" @click="emit('toggle-replay')">
        {{ replayActive ? "停止回放" : "回放轨迹" }}
      </button>
      <input
        type="range"
        :min="0"
        :max="Math.max(latestIndex, 0)"
        :value="activeIndex"
        :disabled="latestIndex <= 0"
        @input="emit('set-active-index', Number($event.target.value))"
      />
      <span>{{ replayLabel }}</span>
    </div>

    <div class="route-footer">
      <div>
        <span>当前位置</span>
        <strong>{{ activePoint ? `${formatNumber(activePoint.lng, 3)}, ${formatNumber(activePoint.lat, 3)}` : "等待采样" }}</strong>
      </div>
      <div>
        <span>剩余路程</span>
        <strong>{{ activePoint ? `${formatNumber(activePoint.remainingKm)} km` : "--" }}</strong>
      </div>
      <div>
        <span>回放状态</span>
        <strong>{{ replayActive ? `查看第 ${activeIndex + 1} 个采样点` : "定位在最新采样" }}</strong>
      </div>
    </div>
  </section>
</template>
