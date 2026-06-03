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

const vehicleStatusOptions = getVehicleStatusOptions();
const vehicleForm = reactive({
  vehicleId: "",
  cargoName: "",
  status: vehicleStatusOptions[0],
  driver: "",
  route: "",
});
const editingVehicleId = ref("");

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
  editingVehicleId.value = "";
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
  editingVehicleId.value = vehicle.vehicleId;
  vehicleForm.vehicleId = vehicle.vehicleId;
  vehicleForm.cargoName = vehicle.cargoName;
  vehicleForm.status = vehicle.status;
  vehicleForm.driver = vehicle.driver;
  vehicleForm.route = vehicle.route;
}

async function submitVehicle() {
  try {
    if (editingVehicleId.value) {
      await updateVehicleForUser(props.currentUser, editingVehicleId.value, vehicleForm);
      setFeedback("success", `已更新我的车辆 ${vehicleForm.vehicleId}。`);
    } else {
      await createVehicleForUser(props.currentUser, vehicleForm);
      setFeedback("success", `已新增我的车辆 ${vehicleForm.vehicleId}。`);
    }
    resetVehicleForm();
    await loadVehiclesWorkspace();
  } catch (error) {
    setFeedback("error", error.message);
  }
}

async function removeVehicle(vehicleId) {
  const confirmed = window.confirm(`确认删除我的车辆 ${vehicleId} 吗？`);
  if (!confirmed) {
    return;
  }

  try {
    await deleteVehicleForUser(props.currentUser, vehicleId);
    if (editingVehicleId.value === vehicleId) {
      resetVehicleForm();
    }
    await loadVehiclesWorkspace();
    setFeedback("success", `已删除我的车辆 ${vehicleId}。`);
  } catch (error) {
    setFeedback("error", error.message);
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
        <p class="topbar-brief">车辆维护独立成页，便于业务用户专注管理自己名下的车辆，不再打断监控首页的浏览流程。</p>
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
            <h3>{{ editingVehicleId ? "编辑我的车辆" : "新增我的车辆" }}</h3>
            <p>当前账号下的车辆由业务用户自行维护。</p>
          </div>
        </div>

        <div class="admin-form-grid">
          <label class="admin-field">
            <span>车辆编号</span>
            <input v-model.trim="vehicleForm.vehicleId" class="admin-input" type="text" placeholder="例如 CC-VA-08" />
          </label>
          <label class="admin-field">
            <span>货物类型</span>
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
          <label class="admin-field admin-field-wide">
            <span>路线</span>
            <input v-model.trim="vehicleForm.route" class="admin-input" type="text" placeholder="例如 北京仓库 -> 区域医院" />
          </label>
        </div>

        <div class="admin-inline-actions">
          <button class="ghost-button primary" type="button" @click="submitVehicle">
            {{ editingVehicleId ? "保存车辆修改" : "新增我的车辆" }}
          </button>
          <button v-if="editingVehicleId" class="ghost-button" type="button" @click="resetVehicleForm">取消编辑</button>
        </div>
      </section>

      <section class="admin-card workspace-card">
        <div class="workspace-card-head">
          <div>
            <h3>车辆列表</h3>
            <p>只展示当前账号名下车辆。</p>
          </div>
        </div>

        <div class="table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>车辆编号</th>
                <th>货物类型</th>
                <th>状态</th>
                <th>司机</th>
                <th>路线</th>
                <th>最近更新</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="vehicle in vehicles" :key="vehicle.vehicleId">
                <td>{{ vehicle.vehicleId }}</td>
                <td>{{ vehicle.cargoName }}</td>
                <td>{{ vehicle.status }}</td>
                <td>{{ vehicle.driver }}</td>
                <td>{{ vehicle.route }}</td>
                <td>{{ vehicle.updatedAt }}</td>
                <td class="admin-actions-cell">
                  <button class="table-action" type="button" @click="editVehicle(vehicle)">编辑</button>
                  <button class="table-action danger" type="button" @click="removeVehicle(vehicle.vehicleId)">删除</button>
                </td>
              </tr>
              <tr v-if="!vehicles.length">
                <td colspan="7">当前账号下还没有车辆，可先新增一辆。</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>
