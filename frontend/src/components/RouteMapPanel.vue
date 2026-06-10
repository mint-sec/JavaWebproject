<script setup>
import { computed } from "vue";
import { formatNumber, mapTrajectoryToCanvas } from "../composables/dashboardUtils";

const props = defineProps({
  trajectory: { type: Array, required: true },
  activePoint: { type: Object, default: null },
  activeIndex: { type: Number, required: true },
  latestIndex: { type: Number, required: true },
  followLatest: { type: Boolean, required: true },
  replayActive: { type: Boolean, required: true },
  routeTone: { type: String, default: "" },
});

const emit = defineEmits(["focus-latest", "toggle-replay", "set-active-index"]);

const mappedPoints = computed(() => mapTrajectoryToCanvas(props.trajectory));
const routePath = computed(() => mappedPoints.value.map((point) => `${point.x},${point.y}`).join(" "));
const latestPoint = computed(() => mappedPoints.value[props.latestIndex] || null);

const replayLabel = computed(() => {
  if (props.latestIndex <= 0) {
    return "至少需要 2 个采样点才能回放";
  }
  if (props.replayActive) {
    return `回放中：第 ${props.activeIndex + 1} / ${props.latestIndex + 1} 个采样点`;
  }
  if (props.followLatest) {
    return `实时跟随最新采样，共 ${props.latestIndex + 1} 个点`;
  }
  return `手动查看第 ${props.activeIndex + 1} / ${props.latestIndex + 1} 个采样点`;
});

const latestMarker = computed(() => {
  if (!latestPoint.value) {
    return null;
  }
  return buildLabel(latestPoint.value, "最新位置");
});

const activeMarker = computed(() => {
  const point = mappedPoints.value[props.activeIndex];
  if (!point || !props.activePoint) {
    return null;
  }
  return buildLabel(point, props.activePoint.sampleTime || "当前查看", true);
});

function buildLabel(point, text, preferAbove = false) {
  const canvasWidth = 720;
  const horizontalOffset = point.x > canvasWidth - 150 ? -16 : 16;
  const anchor = horizontalOffset < 0 ? "end" : "start";
  let y = point.y + (preferAbove ? -14 : 5);

  if (preferAbove && point.y < 36) {
    y = point.y + 24;
  }
  if (!preferAbove && point.y > 350) {
    y = point.y - 14;
  }

  return {
    text,
    x: point.x + horizontalOffset,
    y,
    anchor,
  };
}
</script>

<template>
  <section class="panel panel-map">
    <div class="panel-head">
      <div>
        <p class="panel-kicker">轨迹区域</p>
        <h2>车辆轨迹与采样回看</h2>
      </div>
      <span class="pill" :class="routeTone">
        {{ replayActive ? `回放中 ${activeIndex + 1}/${Math.max(trajectory.length, 1)}` : trajectory.length ? `已采样 ${trajectory.length} 次` : "等待采样" }}
      </span>
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

        <g v-for="(point, index) in mappedPoints" :key="`${point.recordTime}-${index}`">
          <circle
            :cx="point.x"
            :cy="point.y"
            :r="index === latestIndex ? 6 : 4"
            :fill="index === activeIndex ? '#ffbf5a' : '#61d8ff'"
            :opacity="index === activeIndex || index === latestIndex ? 1 : 0.72"
          />
        </g>

        <g v-if="latestPoint && latestMarker">
          <circle :cx="latestPoint.x" :cy="latestPoint.y" r="18" fill="rgba(36,215,173,0.2)" />
          <circle :cx="latestPoint.x" :cy="latestPoint.y" r="10" fill="#24d7ad" filter="url(#traceGlow)" />
          <text :x="latestMarker.x" :y="latestMarker.y" :text-anchor="latestMarker.anchor" fill="#eff7fa" font-size="14">
            {{ latestMarker.text }}
          </text>
        </g>

        <g v-if="mappedPoints[activeIndex] && activeMarker">
          <circle :cx="mappedPoints[activeIndex].x" :cy="mappedPoints[activeIndex].y" r="14" fill="rgba(255,191,90,0.18)" />
          <circle :cx="mappedPoints[activeIndex].x" :cy="mappedPoints[activeIndex].y" r="8" fill="#ffbf5a" />
          <text :x="activeMarker.x" :y="activeMarker.y" :text-anchor="activeMarker.anchor" fill="#ffddb0" font-size="14">
            {{ activeMarker.text }}
          </text>
        </g>

        <text x="24" y="32" fill="#88a5b0" font-size="13">轨迹会持续叠加采样点，手动查看历史时不会因轮询而自动跳回最新位置。</text>
      </svg>

      <svg v-else viewBox="0 0 720 380" aria-label="车辆轨迹视图">
        <rect x="0" y="0" width="720" height="380" fill="transparent" />
        <text x="360" y="190" text-anchor="middle" fill="#88a5b0" font-size="18">等待轨迹数据</text>
      </svg>

      <div class="map-legend">
        <span><i class="legend-dot trail"></i>轨迹历史</span>
        <span><i class="legend-dot latest"></i>最新采样</span>
        <span><i class="legend-dot replay"></i>当前查看</span>
      </div>
    </div>

    <div class="replay-toolbar">
      <button class="ghost-button" type="button" :disabled="latestIndex <= 0" @click="emit('toggle-replay')">
        {{ replayActive ? "停止回放" : "回放轨迹" }}
      </button>
      <button class="ghost-button" type="button" :disabled="followLatest || !trajectory.length" @click="emit('focus-latest')">回到最新</button>
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
        <span>查看模式</span>
        <strong>{{ replayActive ? "轨迹回放中" : followLatest ? "跟随最新采样" : "手动查看历史" }}</strong>
      </div>
    </div>
  </section>
</template>
