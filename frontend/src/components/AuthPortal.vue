<script setup>
import { reactive, ref } from "vue";
import { loginUser, registerUser } from "../services/authService";

const emit = defineEmits(["auth-success"]);

const mode = ref("login");
const loading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");

const loginForm = reactive({
  account: "",
  password: "",
});

const registerForm = reactive({
  username: "",
  phone: "",
  email: "",
  password: "",
  confirmPassword: "",
});

async function submitLogin() {
  loading.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const session = await loginUser(loginForm);
    successMessage.value = "登录成功，正在进入系统。";
    emit("auth-success", session);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function submitRegister() {
  loading.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const session = await registerUser(registerForm);
    successMessage.value = "注册成功，当前账号已自动登录。";
    emit("auth-success", session);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="auth-standalone">
    <div class="auth-logo">
      <span class="auth-logo-mark">CC</span>
      <div class="auth-logo-copy">
        <strong>冷链智控平台</strong>
        <small>Cold Chain Control</small>
      </div>
    </div>

    <div class="auth-panel auth-panel-compact">
      <div class="auth-panel-head">
        <h1>{{ mode === "login" ? "登录系统" : "注册账号" }}</h1>
      </div>

      <div class="auth-tabs">
        <button :class="['auth-tab', { active: mode === 'login' }]" type="button" @click="mode = 'login'">登录</button>
        <button :class="['auth-tab', { active: mode === 'register' }]" type="button" @click="mode = 'register'">
          注册
        </button>
      </div>

      <form v-if="mode === 'login'" class="auth-form" @submit.prevent="submitLogin">
        <label>
          <span>用户名</span>
          <input v-model.trim="loginForm.account" type="text" placeholder="请输入用户名" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </label>
        <button class="auth-submit" type="submit" :disabled="loading">
          {{ loading ? "登录中..." : "立即登录" }}
        </button>
      </form>

      <form v-else class="auth-form" @submit.prevent="submitRegister">
        <label>
          <span>用户名</span>
          <input v-model.trim="registerForm.username" type="text" placeholder="请输入用户名" />
        </label>
        <label>
          <span>手机号</span>
          <input v-model.trim="registerForm.phone" type="tel" placeholder="请输入手机号" />
        </label>
        <label>
          <span>邮箱</span>
          <input v-model.trim="registerForm.email" type="email" placeholder="请输入邮箱地址" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="registerForm.password" type="password" placeholder="至少 6 位字符" />
        </label>
        <label>
          <span>确认密码</span>
          <input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" />
        </label>
        <button class="auth-submit" type="submit" :disabled="loading">
          {{ loading ? "注册中..." : "创建账号" }}
        </button>
      </form>

      <p v-if="errorMessage" class="auth-feedback error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="auth-feedback success">{{ successMessage }}</p>
    </div>
  </section>
</template>
