<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createActionRuntime } from './actions/ActionRuntime'
import { authState, initializeAuth, logout } from './auth'
import { getCustomerDetailCards, getHomeCards } from './api'
import AuthPage from './components/AuthPage.vue'
import DynamicCard from './components/DynamicCard.vue'
import FormDrawer from './components/FormDrawer.vue'
import SecurityAdmin from './components/SecurityAdmin.vue'
import type { ActionResult, CardAction, CardPageDefinition, OpenFormRequest } from './types'

type PageKind = 'home' | 'customer' | 'orders'
type SecuritySection = 'users' | 'roles' | 'permissions' | 'menus'

const securitySections: ReadonlyArray<{ key: SecuritySection; label: string }> = [
  { key: 'users', label: '用户管理' },
  { key: 'roles', label: '角色管理' },
  { key: 'permissions', label: '权限配置' },
  { key: 'menus', label: '菜单配置' }
]

const route = useRoute()
const router = useRouter()
const userId = computed(() => authState.account?.userId ?? '')
const page = ref<CardPageDefinition | null>(null)
const error = ref('')
const loading = ref(true)
// key 为卡片 code。动作完成后只递增受影响卡片的 token，实现局部刷新。
const refreshTokens = ref<Record<string, number>>({})
const formOpen = ref(false)
const formRequest = ref<OpenFormRequest | null>(null)
const toast = ref('')
let toastTimer: ReturnType<typeof setTimeout> | undefined

const activePage = computed<PageKind>(() => {
  if (route.name === 'customer-detail') return 'customer'
  if (route.name === 'order-list') return 'orders'
  return 'home'
})
const isAuthRoute = computed(() => route.name === 'login' || route.name === 'register')
const isSecurityAdmin = computed(() => route.name === 'security-admin')
const activeSecuritySection = computed<SecuritySection>(() => {
  const section = String(route.query.section ?? 'users') as SecuritySection
  return securitySections.some(item => item.key === section) ? section : 'users'
})
const activeSecurityLabel = computed(() =>
  securitySections.find(item => item.key === activeSecuritySection.value)?.label ?? '用户管理'
)
const customerId = computed(() => String(route.query.customerId ?? '1001'))
const pageCode = computed(() => (activePage.value === 'customer' ? 'customer_detail' : 'home'))

const pageContext = computed<Record<string, string>>(() =>
  activePage.value === 'customer'
    ? Object.fromEntries([['customerId', customerId.value]])
    : activePage.value === 'orders'
      ? Object.fromEntries(
          Object.entries(route.query).map(([key, value]) => [key, String(value ?? '')])
        )
      : Object.fromEntries([['userId', userId.value]])
)
const heading = computed(() => {
  if (activePage.value === 'customer') return '客户详情'
  if (activePage.value === 'orders') return '客户订单'
  return '首页'
})

const executeCardAction = createActionRuntime(router, {
  refreshCards,
  openForm(request) {
    formRequest.value = request
    formOpen.value = true
  },
  notify
})

async function loadPage() {
  // App 只获取页面级元数据；每张卡片的数据由 DynamicCard 按加载策略独立请求。
  if (!authState.account || isAuthRoute.value || isSecurityAdmin.value) {
    page.value = null
    loading.value = false
    return
  }
  if (activePage.value === 'orders') {
    page.value = null
    error.value = ''
    loading.value = false
    return
  }
  loading.value = true
  error.value = ''
  page.value = null

  try {
    page.value =
      activePage.value === 'customer'
        ? await getCustomerDetailCards(customerId.value)
        : await getHomeCards()
  } catch (reason) {
    error.value = (reason as Error).message
  } finally {
    loading.value = false
  }
}

function switchPage(kind: 'home' | 'customer') {
  if (activePage.value === kind) {
    return
  }
  router.push(
    kind === 'customer'
      ? { name: 'customer-detail', query: { customerId: customerId.value } }
      : { name: 'home' }
  )
}

function navigateMenu(path: string) {
  router.push(path)
}

function navigateSecuritySection(section: SecuritySection) {
  router.push({ name: 'security-admin', query: { section } })
}

async function signOut() {
  await logout()
  await router.replace('/login')
}

function refreshCards(cardCodes: string[]) {
  for (const cardCode of cardCodes) {
    refreshTokens.value[cardCode] = (refreshTokens.value[cardCode] ?? 0) + 1
  }
}

function notify(message: string) {
  toast.value = message
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 3000)
}

function handleCardAction(action: CardAction, cardData: unknown, cardCode: string) {
  // 将声明式动作交给运行时处理，页面只提供当前上下文和卡片数据。
  executeCardAction(action, {
    pageCode: pageCode.value,
    cardCode,
    pageContext: pageContext.value,
    cardData
  })
}

function handleFormSaved(result: ActionResult) {
  refreshCards(result.refreshCards)
  notify(result.message)
}

onMounted(async () => {
  await initializeAuth()
  if (!authState.account && !isAuthRoute.value) await router.replace('/login')
  if (authState.account && isAuthRoute.value) await router.replace('/')
  await loadPage()
})

watch(() => route.fullPath, async () => {
  if (authState.initialized && !authState.account && !isAuthRoute.value) {
    await router.replace('/login')
    return
  }
  await loadPage()
})
</script>

<template>
  <div v-if="!authState.initialized" class="boot-state">正在初始化账号与权限…</div>
  <AuthPage v-else-if="!authState.account || isAuthRoute" />
  <div v-else class="app-shell">
    <header class="topbar">
      <div class="brand-block">
        <span class="brand-mark">M</span>
        <span><b>Metadata UI</b><small>业务能力工作台</small></span>
      </div>
      <div class="topbar-actions">
        <button v-if="!isSecurityAdmin" class="icon-button" title="刷新当前页面" @click="loadPage">↻</button>
        <div class="top-account">
          <span class="mini-avatar">{{ authState.account.displayName.slice(0, 1) }}</span>
          <span><b>{{ authState.account.displayName }}</b><small>{{ authState.account.username }}</small></span>
        </div>
        <button class="logout-button" @click="signOut">退出</button>
      </div>
    </header>

    <div class="workspace">
      <aside class="left-sidebar">
        <div class="sidebar-heading"><span>工作空间</span><small>{{ authState.menus.length }} 个入口</small></div>
        <nav class="side-menu" aria-label="主菜单">
          <template v-for="menu in authState.menus" :key="menu.id">
            <button class="primary-menu" :class="{ active: route.path === menu.path.split('?')[0] }"
                    @click="navigateMenu(menu.path)">
              <span class="menu-icon">{{ menu.name.slice(0, 1) }}</span>
              <span class="menu-copy"><b>{{ menu.name }}</b><small>{{ menu.code }}</small></span>
              <span class="menu-arrow">›</span>
            </button>
            <div v-if="isSecurityAdmin && menu.path.split('?')[0] === '/admin/security'"
                 class="side-submenu" aria-label="权限管理二级菜单">
              <button v-for="item in securitySections" :key="item.key"
                      :class="{ active: activeSecuritySection === item.key }"
                      @click="navigateSecuritySection(item.key)">
                <span class="submenu-dot" />{{ item.label }}
              </button>
            </div>
          </template>
        </nav>
        <div class="sidebar-foot">
          <span class="status-dot" />
          <span><b>服务正常</b><small>MySQL · JWT · RBAC</small></span>
        </div>
      </aside>

      <section class="center-stage">
        <nav class="page-directory" aria-label="页面目录">
          <span>工作空间</span><i>/</i><strong>{{ isSecurityAdmin ? '权限管理' : heading }}</strong>
          <template v-if="isSecurityAdmin"><i>/</i><strong>{{ activeSecurityLabel }}</strong></template>
        </nav>
        <SecurityAdmin v-if="isSecurityAdmin" />
        <main v-else class="business-main">
          <div v-if="loading" class="page-state">正在装配页面卡片…</div>
          <div v-else-if="error" class="page-state error">
            {{ error }}
            <button @click="loadPage">重试</button>
          </div>
          <section v-else-if="page" class="grid" :style="{ '--gap': `${page.layout.gap}px` }">
            <!--
              后端返回的每项 CardDefinition 在这里装配成 DynamicCard：
              component 决定展示组件，span 决定响应式宽度，actions 决定可用操作。
            -->
            <DynamicCard
              v-for="card in page.cards"
              :key="card.code"
              :definition="card"
              :context="pageContext"
              :refresh-token="refreshTokens[card.code] ?? 0"
              :style="{ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }"
              @action="(action, data) => handleCardAction(action, data, card.code)"
            />
            <div v-if="page.cards.length === 0" class="page-state empty">当前没有可用卡片</div>
          </section>
          <section v-else-if="activePage === 'orders'" class="orders-placeholder">
            <h2>动态参数跳转成功</h2>
            <p>当前页面从卡片动作接收到以下查询参数：</p>
            <pre>{{ JSON.stringify(pageContext, null, 2) }}</pre>
            <button @click="switchPage('customer')">返回客户详情</button>
          </section>
        </main>
      </section>

    </div>

    <footer class="app-footer">
      <span>Metadata UI Platform</span>
      <span>协议 v1.0 · Spring Boot 2.7 · Vue 3</span>
      <span>© 2026 Demo Workspace</span>
    </footer>
  </div>

  <FormDrawer v-model="formOpen" :request="formRequest" @saved="handleFormSaved" />
  <div v-if="toast" class="toast">{{ toast }}</div>
</template>

<style>
* { box-sizing: border-box; }
:root { color-scheme: light; font-family: Inter, "PingFang SC", "Microsoft YaHei", sans-serif; }
body { margin: 0; background: #eef2f7; color: #182235; }
button { font: inherit; }
.boot-state { min-height: 100vh; display: grid; place-items: center; color: #64748b; }
.app-shell { min-height: 100vh; display: grid; grid-template-rows: 68px minmax(0, 1fr) 46px; }
.topbar { position: sticky; z-index: 100; top: 0; display: grid; grid-template-columns: 230px 1fr;
  align-items: center; gap: 24px; padding: 0 24px; border-bottom: 1px solid #dfe6ef; background: rgba(255,255,255,.94); backdrop-filter: blur(14px); }
.brand-block,.top-account { display: flex; align-items: center; gap: 11px; }.brand-block > span:last-child,.top-account > span:last-child { display: grid; gap: 2px; }
.brand-block b { color: #12203b; font-size: 15px; }.brand-block small,.top-account small { color: #8a98ad; font-size: 11px; }
.brand-mark { display: grid; width: 36px; height: 36px; place-items: center; border-radius: 11px; background: linear-gradient(135deg,#1d4ed8,#60a5fa); color: white; font-weight: 900; box-shadow: 0 7px 18px #2563eb32; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; background: #22c55e; box-shadow: 0 0 0 4px #dcfce7; }
.topbar-actions { display: flex; align-items: center; justify-self: end; gap: 12px; }.icon-button,.logout-button { border: 0; border-radius: 9px; cursor: pointer; }
.icon-button { width: 34px; height: 34px; background: #f1f5f9; color: #52627a; font-size: 18px; }.logout-button { padding: 7px 11px; background: transparent; color: #64748b; }
.mini-avatar { display: grid; width: 34px; height: 34px; place-items: center; border-radius: 10px; background: #e0e7ff; color: #3730a3; font-weight: 800; }.top-account b { color: #334155; font-size: 12px; }
.workspace { display: grid; min-width: 0; grid-template-columns: 230px minmax(0,1fr); }
.left-sidebar { position: sticky; top: 68px; display: flex; height: calc(100vh - 114px); flex-direction: column; padding: 22px 15px 16px; border-right: 1px solid #dfe6ef; background: #f8fafc; }
.sidebar-heading { display: flex; align-items: center; justify-content: space-between; padding: 0 9px 13px; color: #52627a; font-size: 12px; font-weight: 700; }.sidebar-heading small { color: #a0aec0; font-weight: 500; }
.side-menu { display: grid; gap: 6px; }.primary-menu { display: grid; grid-template-columns: 34px 1fr auto; align-items: center; gap: 10px; padding: 10px; border: 1px solid transparent; border-radius: 11px; background: transparent; color: #52627a; cursor: pointer; text-align: left; }
.primary-menu:hover { background: #eef3fa; }.primary-menu.active { border-color: #cbdcff; background: #e8f0ff; color: #1d4ed8; box-shadow: inset 3px 0 #2563eb; }
.menu-icon { display: grid; width: 32px; height: 32px; place-items: center; border-radius: 9px; background: white; box-shadow: 0 2px 7px #22345512; color: #64748b; font-size: 12px; font-weight: 800; }.active .menu-icon { background: #2563eb; color: white; }
.menu-copy { display: grid; gap: 2px; }.menu-copy b { font-size: 13px; }.menu-copy small { color: #9aa7b8; font-size: 10px; }.menu-arrow { color: #a7b2c1; font-size: 18px; }
.side-submenu { display: grid; gap: 3px; margin: -1px 8px 5px 25px; padding: 4px 0 4px 14px; border-left: 1px solid #d7e0eb; }
.side-submenu button { display: flex; align-items: center; gap: 9px; padding: 8px 10px; border: 0; border-radius: 8px;
  background: transparent; color: #718096; cursor: pointer; font-size: 12px; text-align: left; }
.side-submenu button:hover { background: #eef3fa; color: #334155; }.side-submenu button.active { background: #e8f0ff; color: #1d4ed8; font-weight: 700; }
.submenu-dot { width: 5px; height: 5px; border-radius: 50%; background: #b5c0cf; }.side-submenu button.active .submenu-dot { background: #2563eb; box-shadow: 0 0 0 3px #dbeafe; }
.sidebar-foot { display: flex; align-items: center; gap: 11px; margin-top: auto; padding: 14px 12px; border: 1px solid #e3e9f1; border-radius: 12px; background: white; }.sidebar-foot > span:last-child { display: grid; gap: 2px; }.sidebar-foot b { color: #3f4d63; font-size: 12px; }.sidebar-foot small { color: #98a5b6; font-size: 10px; }
.center-stage { min-width: 0; padding: 24px; }.business-main { max-width: 1160px; margin: 0 auto; }
.page-directory { display: flex; max-width: 1160px; align-items: center; gap: 7px; margin: 0 auto 18px;
  padding: 0 2px 13px; border-bottom: 1px solid #dfe6ef; color: #94a3b8; font-size: 13px; }
.page-directory i { color: #cbd5e1; font-style: normal; }.page-directory strong { color: #334155; font-weight: 700; }
.grid { display: grid; grid-template-columns: repeat(24,1fr); gap: var(--gap); }.grid > article { grid-column: span var(--xl); }
.page-state { padding: 70px; color: #718096; text-align: center; }.page-state.error { color: #b42318; }.page-state button { margin-left: 10px; }.page-state.empty { grid-column: 1/-1; }
.orders-placeholder { padding: 32px; border: 1px solid #e2e8f0; border-radius: 14px; background: white; box-shadow: 0 7px 22px #192b460e; }.orders-placeholder h2 { margin-top: 0; }.orders-placeholder pre { overflow: auto; padding: 16px; border-radius: 8px; background: #f1f5f9; color: #334155; }.orders-placeholder button { padding: 9px 14px; border: 0; border-radius: 8px; background: #2563eb; color: white; cursor: pointer; }
.app-footer { display: grid; grid-template-columns: 230px 1fr auto; align-items: center; gap: 20px; padding: 0 24px; border-top: 1px solid #dfe6ef; background: #f8fafc; color: #8a98ad; font-size: 10px; }.app-footer span:nth-child(2) { text-align: center; }
.toast { position: fixed; z-index: 1100; right: 24px; bottom: 62px; padding: 12px 18px; border-radius: 9px; background: #172033; box-shadow: 0 8px 24px #0f172a33; color: white; }
@media (max-width: 1240px) { .workspace { grid-template-columns: 220px minmax(0,1fr); }.topbar { grid-template-columns: 220px 1fr; } }
@media (max-width: 860px) { .app-shell { grid-template-rows: auto minmax(0,1fr) 46px; }.topbar { position: static; grid-template-columns: 1fr auto; min-height: 66px; }.workspace { display: block; }.left-sidebar { position: static; height: auto; padding: 10px 16px; border-right: 0; border-bottom: 1px solid #dfe6ef; }.sidebar-heading,.sidebar-foot { display: none; }.side-menu { display: flex; overflow-x: auto; }.primary-menu { min-width: 150px; grid-template-columns: 30px 1fr; }.side-submenu { display: flex; min-width: max-content; margin: 0; padding: 0 0 0 8px; border-left: 1px solid #d7e0eb; }.side-submenu button { min-width: auto; white-space: nowrap; }.menu-arrow { display: none; }.center-stage { padding: 18px; }.app-footer { grid-template-columns: 1fr auto; }.app-footer span:first-child { display: none; } }
@media (max-width: 600px) { .topbar { padding: 0 14px; }.brand-block small,.top-account > span:last-child,.icon-button { display: none; }.topbar-actions { gap: 5px; }.center-stage { padding: 13px; }.page-directory { margin-bottom: 13px; padding-bottom: 10px; }.grid > article { grid-column: span var(--xs); }.app-footer { display: flex; justify-content: center; }.app-footer span:last-child { display: none; }.page-state { padding: 45px 10px; } }
</style>
