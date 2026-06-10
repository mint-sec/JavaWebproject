<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  createVehicleForUser,
  deleteVehicleForUser,
  getBusinessWorkspaceData,
  getVehicleStatusOptions,
  updateVehicleForUser,
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
const submittingVehicle = ref(false);
const deletingVehicleKey = ref("");

const vehicleStatusOptions = getVehicleStatusOptions();
const vehicleForm = reactive({
  vehicleId: "",
  cargoName: "",
  status: vehicleStatusOptions[0],
  driver: "",
  route: "",
  routeDistanceKm: 30,
});
const editingVehicleKey = ref("");

const overviewCards = computed(() => {
  const cards = data.value?.overviewCards || [];
  return cards.filter((card) => ["我的车辆", "待处理告警"].includes(card.label));
});
const vehicles = computed(() => data.value?.vehicles || []);

function setFeedback(type, text) {
  feedback.type = type;
  feedback.text = text;
}

function resetVehicleForm() {
  vehicleForm.vehicleId = "";
  vehicleForm.cargoName = "";
  vehicleForm.status = vehicleStatusOptions[0];
  vehicleForm.driver = "";
  vehicleForm.route = "";
  vehicleForm.routeDistanceKm = 30;
  editingVehicleKey.value = "";
}

function validateVehicleForm() {
  if (!vehicleForm.vehicleId.trim()) {
    throw new Error("请输入车辆编号。");
  }
  if (!vehicleForm.cargoName.trim()) {
    throw new Error("请输入货物名称。");
  }
  if (!vehicleForm.driver.trim()) {
    throw new Error("请输入司机姓名。");
  }
  if (!vehicleForm.route.trim()) {
    throw new Error("请输入运输路线。");
  }
  if (!Number(vehicleForm.routeDistanceKm) || Number(vehicleForm.routeDistanceKm) <= 0) {
    throw new Error("请输入大于 0 的总路程。");
  }
}

async function loadVehiclesWorkspace() {
  loading.value = true;
  try {
    data.value = await getBusinessWorkspaceData(props.currentUser);
  } catch (error) {
    setFeedback("error", error.message || "车辆数据加载失败，请稍后重试。");
  } finally {
    loading.value = false;
  }
}

function editVehicle(vehicle) {
  editingVehicleKey.value = vehicle.vehicleKey;
  vehicleForm.vehicleId = vehicle.vehicleId;
  vehicleForm.cargoName = vehicle.cargoName;
  vehicleForm.status = vehicle.status;
  vehicleForm.driver = vehicle.driver;
  vehicleForm.route = vehicle.route;
  vehicleForm.routeDistanceKm = vehicle.routeDistanceKm || 30;
}

async function submitVehicle() {
  if (submittingVehicle.value) {
    return;
  }
  submittingVehicle.value = true;
  try {
    validateVehicleForm();
    const payload = {
      vehicleId: vehicleForm.vehicleId.trim(),
      cargoName: vehicleForm.cargoName.trim(),
      status: vehicleForm.status,
      driver: vehicleForm.driver.trim(),
      route: vehicleForm.route.trim(),
      routeDistanceKm: Number(vehicleForm.routeDistanceKm),
    };

    if (editingVehicleKey.value) {
      await updateVehicleForUser(props.currentUser, editingVehicleKey.value, payload);
      setFeedback("success", `已更新我的车辆 ${payload.vehicleId}。`);
    } else {
      await createVehicleForUser(props.currentUser, payload);
      setFeedback("success", `已新增我的车辆 ${payload.vehicleId}，系统会立即开始生成模拟监控数据。`);
    }
    resetVehicleForm();
    await loadVehiclesWorkspace();
  } catch (error) {
    setFeedback("error", error.message);
  } finally {
    submittingVehicle.value = false;
  }
}

async function removeVehicle(vehicle) {
  if (deletingVehicleKey.value) {
    return;
  }
  const confirmed = window.confirm(`确认删除我的车辆 ${vehicle.vehicleId} 吗？`);
  if (!confirmed) {
    return;
  }

  deletingVehicleKey.value = vehicle.vehicleKey;
  try {
    await deleteVehicleForUser(props.currentUser, vehicle.vehicleKey);
    if (editingVehicleKey.value === vehicle.vehicleKey) {
      resetVehicleForm();
    }
    await loadVehiclesWorkspace();
    setFeedback("success", `已删除我的车辆 ${vehicle.vehicleId}。`);
  } catch (error) {
    setFeedback("error", error.message);
  } finally {
    deletingVehicleKey.value = "";
  }
}

onMounted(() => {
  loadVehiclesWorkspace();
});
</script>

<template>
  <section class="workspace-section">
    <div class="workspace-head">
      <div>
        <p class="panel-kicker">业务中心</p>
        <h1>我的车辆</h1>
        <p class="topbar-brief">在这里维护当前账号名下的车辆信息。车辆创建成功后，监控大屏会立即拿到该车辆的模拟实时数据。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadVehiclesWorkspace">
        {{ loading ? "刷新中..." : "刷新车辆数据" }}
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

    <div class="workspace-grid">
      <section class="admin-card workspace-card">
        <div class="workspace-card-head">
          <div>
            <h3>{{ editingVehicleKey ? "编辑我的车辆" : "新增我的车辆" }}</h3>
            <p>同一账号下车辆编号不能重复，但不同账号可以使用相同的车辆编号。</p>
          </div>
        </div>

        <div class="admin-form-grid">
          <label class="admin-field">
            <span>车辆编号</span>
            <input v-model.trim="vehicleForm.vehicleId" class="admin-input" type="text" placeholder="例如 CC-VA-08" />
          </label>
          <label class="admin-field">
            <span>货物名称</span>
            <input v-model.trim="vehicleForm.cargoName" class="admin-input" type="text" placeholder="例如 疫苗" />
          </label>
          <label class="admin-field">
            <span>车辆状态</span>
            <select v-model="vehicleForm.status" class="admin-select">
              <option v-for="status in vehicleStatusOptions" :key="status" :value="status">{{ status }}</option>
            </select>
          </label>
          <label class="admin-field">
            <span>司机</span>
            <input v-model.trim="vehicleForm.driver" class="admin-input" type="text" placeholder="请输入司机姓名" />
          </label>
          <label class="admin-field">
            <span>总路程（km）</span>
            <input v-model.number="vehicleForm.routeDistanceKm" class="admin-input" type="number" min="1" step="0.1" placeholder="例如 30" />
          </label>
          <label class="admin-field admin-field-wide">
            <span>运输路线</span>
            <input v-model.trim="vehicleForm.route" class="admin-input" type="text" placeholder="例如 北京仓库 -> 区域医院" />
          </label>
        </div>

        <div class="admin-inline-actions">
          <button class="ghost-button primary" type="button" :disabled="submittingVehicle" @click="submitVehicle">
            {{ submittingVehicle ? "提交中..." : editingVehicleKey ? "保存车辆修改" : "新增我的车辆" }}
          </button>
          <button v-if="editingVehicleKey" class="ghost-button" type="button" @click="resetVehicleForm">取消编辑</button>
        </div>
      </section>

      <section class="admin-card workspace-card">
        <div class="workspace-card-head">
          <div>
            <h3>车辆列表</h3>
            <p>这里只显示当前账号归属的车辆。</p>
          </div>
        </div>

        <div class="table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>车辆编号</th>
                <th>货物名称</th>
                <th>状态</th>
                <th>司机</th>
                <th>总路程</th>
                <th>路线</th>
                <th>最近更新</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="vehicle in vehicles" :key="vehicle.vehicleKey">
                <td>{{ vehicle.vehicleId }}</td>
                <td>{{ vehicle.cargoName }}</td>
                <td>{{ vehicle.status }}</td>
                <td>{{ vehicle.driver }}</td>
                <td>{{ vehicle.routeDistanceKm }} km</td>
                <td>{{ vehicle.route }}</td>
                <td>{{ vehicle.updatedAt }}</td>
                <td class="admin-actions-cell">
                  <button class="table-action" type="button" @click="editVehicle(vehicle)">编辑</button>
                  <button
                    class="table-action danger"
                    type="button"
                    :disabled="deletingVehicleKey === vehicle.vehicleKey"
                    @click="removeVehicle(vehicle)"
                  >
                    {{ deletingVehicleKey === vehicle.vehicleKey ? "删除中..." : "删除" }}
                  </button>
                </td>
              </tr>
              <tr v-if="!vehicles.length">
                <td colspan="8">当前账号下还没有车辆，可以先新增一辆。</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>
