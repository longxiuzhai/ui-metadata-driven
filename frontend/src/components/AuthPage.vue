<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login, register } from '../auth'

const route = useRoute()
const router = useRouter()
const username = ref('admin')
const password = ref('Admin123!')
const displayName = ref('')
const email = ref('')
const submitting = ref(false)
const error = ref('')
const isRegister = computed(() => route.name === 'register')

async function submit() {
  submitting.value = true
  error.value = ''
  try {
    if (isRegister.value) {
      await register(username.value, password.value, displayName.value, email.value)
    } else {
      await login(username.value, password.value)
    }
    await router.replace('/')
  } catch (reason) {
    error.value = (reason as Error).message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <form class="auth-card" @submit.prevent="submit">
      <span class="brand">METADATA UI</span>
      <h1>{{ isRegister ? '创建账号' : '欢迎回来' }}</h1>
      <p>{{ isRegister ? '注册后自动获得普通用户角色' : '登录后按角色加载菜单和卡片权限' }}</p>

      <label v-if="isRegister">
        <span>显示名称</span>
        <input v-model="displayName" required maxlength="100" autocomplete="name" />
      </label>
      <label>
        <span>用户名</span>
        <input v-model="username" required maxlength="32" autocomplete="username" />
      </label>
      <label v-if="isRegister">
        <span>邮箱（可选）</span>
        <input v-model="email" type="email" maxlength="150" autocomplete="email" />
      </label>
      <label>
        <span>密码</span>
        <input v-model="password" type="password" required minlength="8"
               autocomplete="current-password" />
      </label>

      <div v-if="error" class="auth-error">{{ error }}</div>
      <button class="primary" :disabled="submitting">
        {{ submitting ? '处理中…' : isRegister ? '注册并登录' : '登录' }}
      </button>
      <button type="button" class="link" @click="router.push(isRegister ? '/login' : '/register')">
        {{ isRegister ? '已有账号？去登录' : '没有账号？立即注册' }}
      </button>
      <small v-if="!isRegister">初始管理员：admin / Admin123!</small>
    </form>
  </div>
</template>

<style scoped>
.auth-page { min-height: 100vh; display: grid; place-items: center; padding: 24px;
  background: radial-gradient(circle at top left, #dbeafe, transparent 38%), #f4f7fb; }
.auth-card { width: min(420px, 100%); display: grid; gap: 17px; padding: 38px;
  border: 1px solid #e2e8f0; border-radius: 20px; background: white;
  box-shadow: 0 24px 70px rgba(30, 64, 175, .12); }
.brand { color: #2563eb; font-size: 12px; font-weight: 800; letter-spacing: .15em; }
h1 { margin: 0; font-size: 30px; } p { margin: -8px 0 5px; color: #64748b; }
label { display: grid; gap: 7px; } label span { color: #475569; font-size: 13px; font-weight: 650; }
input { width: 100%; padding: 11px 12px; border: 1px solid #cbd5e1; border-radius: 9px; font: inherit; }
button { border: 0; cursor: pointer; font: inherit; } button:disabled { opacity: .55; }
.primary { padding: 12px; border-radius: 9px; background: #2563eb; color: white; font-weight: 700; }
.link { background: transparent; color: #2563eb; } .auth-error { color: #b42318; font-size: 14px; }
small { color: #94a3b8; text-align: center; }
</style>
