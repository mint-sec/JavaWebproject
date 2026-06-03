<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import AuthPortal from "./components/AuthPortal.vue";
import DashboardView from "./components/DashboardView.vue";
import AdminConsole from "./components/AdminConsole.vue";
import MyVehiclesView from "./components/MyVehiclesView.vue";
import MyAlertsView from "./components/MyAlertsView.vue";
import { getCurrentSession, hasAdminRole, logoutUser, onSessionChanged } from "./services/authService";

let offSessionChange = null;

const session = ref(getCurrentSession());
const currentView = ref(resolveView(window.location.hash, session.value));

const canAccessAdmin = computed(() => hasAdminRole(session.value));
const mainNavItems = computed(() => {
  return [
    { id: "dashboard", label: "工作台" },
    { id: "vehicles", label: "我的车辆" },
    { id: "alerts", label: "我的告警" },
  ];
});
const shellTitle = computed(() => {
  if (!session.value) {
    return "账号入口";
  }
  if (currentView.value === "admin") {
    return "后台管理";
  }
  if (currentView.value === "vehicles") {
    return "我的车辆";
  }
  if (currentView.value === "alerts") {
    return "我的告警";
  }
  return "工作台";
});

function resolveView(hash, activeSession) {
  const normalized = String(hash || "").replace(/^#\/?/, "");

  if (!activeSession) {
    return "auth";
  }

  if (normalized === "admin" && hasAdminRole(activeSession)) {
    return "admin";
  }

  if (normalized === "vehicles") {
    return "vehicles";
  }

  if (normalized === "alerts") {
    return "alerts";
  }

  if (normalized === "dashboard") {
    return "dashboard";
  }

  return "dashboard";
}

function syncViewFromHash() {
  currentView.value = resolveView(window.location.hash, session.value);
}

function navigate(view) {
  if (view === "admin" && !canAccessAdmin.value) {
    view = "dashboard";
  }
  if (view === "auth") {
    window.location.hash = "/auth";
    currentView.value = "auth";
    return;
  }
  window.location.hash = `/${view}`;
  currentView.value = view;
}

function handleAuthSuccess(nextSession) {
  session.value = nextSession;
  navigate("dashboard");
}

function handleLogout() {
  logoutUser();
  session.value = null;
  navigate("auth");
}

function handleSessionRefresh() {
  session.value = getCurrentSession();
  if (!session.value) {
    navigate("auth");
    return;
  }
  syncViewFromHash();
}

onMounted(() => {
  window.addEventListener("hashchange", syncViewFromHash);
  offSessionChange = onSessionChanged(handleSessionRefresh);
  if (!window.location.hash) {
    navigate(currentView.value);
  } else {
    syncViewFromHash();
  }
});

onBeforeUnmount(() => {
  window.removeEventListener("hashchange", syncViewFromHash);
  if (typeof offSessionChange === "function") {
    offSessionChange();
    offSessionChange = null;
  }
});
</script>

<template>
  <div class="portal-shell">
    <div v-if="session" class="portal-header">
      <div class="portal-header-main">
        <h2>{{ shellTitle }}</h2>
        <nav class="portal-nav portal-nav-main" aria-label="主导航">
          <button
            v-for="item in mainNavItems"
            :key="item.id"
            :class="['portal-nav-item', { active: currentView === item.id }]"
            type="button"
            @click="navigate(item.id)"
          >
            {{ item.label }}
          </button>
        </nav>
      </div>

      <div class="portal-actions">
        <button v-if="canAccessAdmin && currentView !== 'admin'" class="ghost-button" type="button" @click="navigate('admin')">
          进入后台
        </button>
        <div class="portal-user">
          <strong>{{ session.displayName }}</strong>
          <span>{{ session.roleLabel }}</span>
        </div>

        <button class="ghost-button" type="button" @click="handleLogout">退出登录</button>
      </div>
    </div>

    <AuthPortal v-if="!session" @auth-success="handleAuthSuccess" />

    <AdminConsole
      v-else-if="currentView === 'admin'"
      :current-user="session"
      @open-dashboard="navigate('dashboard')"
    />

    <MyVehiclesView v-else-if="currentView === 'vehicles'" :current-user="session" />

    <MyAlertsView v-else-if="currentView === 'alerts'" :current-user="session" />

    <DashboardView
      v-else
      :current-user="session"
      :can-access-admin="canAccessAdmin"
      @open-admin="navigate('admin')"
      @open-vehicles="navigate('vehicles')"
      @logout="handleLogout"
    />
  </div>
</template>
