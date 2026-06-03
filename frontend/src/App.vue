<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import AuthPortal from "./components/AuthPortal.vue";
import DashboardView from "./components/DashboardView.vue";
import AdminConsole from "./components/AdminConsole.vue";
import { getCurrentSession, hasAdminRole, logoutUser, onSessionChanged } from "./services/authService";

let offSessionChange = null;

const session = ref(getCurrentSession());
const currentView = ref(resolveView(window.location.hash, session.value));

const canAccessAdmin = computed(() => hasAdminRole(session.value));
const shellTitle = computed(() => {
  if (!session.value) {
    return "账号入口";
  }
  return currentView.value === "admin" ? "后台管理" : "工作台";
});

function resolveView(hash, activeSession) {
  const normalized = String(hash || "").replace(/^#\/?/, "");

  if (!activeSession) {
    return "auth";
  }

  if (normalized === "admin" && hasAdminRole(activeSession)) {
    return "admin";
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
      <div>
        <h2>{{ shellTitle }}</h2>
      </div>

      <div class="portal-actions">
        <div class="portal-user">
          <strong>{{ session.displayName }}</strong>
          <span>{{ session.roleLabel }}</span>
        </div>
        <button v-if="canAccessAdmin && currentView !== 'admin'" class="ghost-button" type="button" @click="navigate('admin')">
          进入后台
        </button>
        <button v-if="currentView !== 'dashboard'" class="ghost-button" type="button" @click="navigate('dashboard')">
          返回工作台
        </button>
        <button class="ghost-button" type="button" @click="handleLogout">退出登录</button>
      </div>
    </div>

    <AuthPortal v-if="!session" @auth-success="handleAuthSuccess" />

    <AdminConsole
      v-else-if="currentView === 'admin'"
      :current-user="session"
      @open-dashboard="navigate('dashboard')"
    />

    <DashboardView
      v-else
      :current-user="session"
      :can-access-admin="canAccessAdmin"
      @open-admin="navigate('admin')"
      @logout="handleLogout"
    />
  </div>
</template>
