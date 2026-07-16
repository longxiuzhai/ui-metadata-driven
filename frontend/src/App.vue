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

const route = useRoute()
const router = useRouter()
const userId = computed(() => authState.account?.userId ?? '')
const page = ref<CardPageDefinition | null>(null)
const error = ref('')
const loading = ref(true)
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
const currentMenu = computed(() =>
  authState.menus.find(menu => route.path === menu.path.split('?')[0])
)
const permissionPreview = computed(() => authState.account?.permissions.slice(0, 4) ?? [])
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
  return '工作首页'
})
const subtitle = computed(() => {
  if (activePage.value === 'customer') {
    return `客户编号 ${customerId.value} · 卡片由后端能力、租户与权限动态决定`
  }
  if (activePage.value === 'orders') {
    return `客户 ${pageContext.value.customerId ?? '-'} · 状态 ${pageContext.value.status ?? '全部'}`
  }
  return '面向当前用户动态装配工作摘要与业务插件卡片'
})
const avatar = computed(() =>
  activePage.value === 'customer'
    ? customerId.value.slice(-2)
    : activePage.value === 'orders'
      ? 'OR'
      : 'HI'
)

const executeCardAction = createActionRuntime(router, {
  refreshCards,
  openForm(request) {
    formRequest.value = request
    formOpen.value = true
  },
  notify
})

async function loadPage() {
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
      <div class="topbar-context">
        <span class="environment-dot" />
        <span>演示租户</span>
        <i>/</i>
        <strong>{{ currentMenu?.name ?? heading }}</strong>
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

    <div class="workspace" :class="{ 'admin-workspace': isSecurityAdmin }">
      <aside class="left-sidebar">
        <div class="sidebar-heading"><span>工作空间</span><small>{{ authState.menus.length }} 个入口</small></div>
        <nav class="side-menu" aria-label="主菜单">
          <button v-for="menu in authState.menus" :key="menu.id"
                  :class="{ active: route.path === menu.path.split('?')[0] }"
                  @click="navigateMenu(menu.path)">
            <span class="menu-icon">{{ menu.name.slice(0, 1) }}</span>
            <span class="menu-copy"><b>{{ menu.name }}</b><small>{{ menu.code }}</small></span>
            <span class="menu-arrow">›</span>
          </button>
        </nav>
        <div class="sidebar-foot">
          <span class="status-dot" />
          <span><b>服务正常</b><small>MySQL · JWT · RBAC</small></span>
        </div>
      </aside>

      <section class="center-stage">
        <SecurityAdmin v-if="isSecurityAdmin" />
        <main v-else class="business-main">
          <section class="hero">
            <div>
              <div class="breadcrumb"><span>工作台</span><i>›</i><strong>{{ heading }}</strong></div>
              <span class="eyebrow">METADATA-DRIVEN UI · RBAC</span>
              <h1>{{ heading }}</h1>
              <p>{{ subtitle }}</p>
            </div>
            <div class="hero-side">
              <span class="hero-label">当前上下文</span>
              <div class="avatar">{{ avatar }}</div>
            </div>
          </section>

          <div v-if="loading" class="page-state">正在装配页面卡片…</div>
          <div v-else-if="error" class="page-state error">
            {{ error }}
            <button @click="loadPage">重试</button>
          </div>
          <section v-else-if="page" class="grid" :style="{ '--gap': `${page.layout.gap}px` }">
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

      <aside class="right-rail">
        <section class="rail-card account-summary">
          <span class="rail-kicker">当前账号</span>
          <div class="rail-user"><span class="large-avatar">{{ authState.account.displayName.slice(0, 1) }}</span>
            <span><b>{{ authState.account.displayName }}</b><small>{{ authState.account.username }}</small></span></div>
          <div class="rail-metrics"><span><b>{{ authState.account.roles.length }}</b><small>角色</small></span>
            <span><b>{{ authState.account.permissions.length }}</b><small>权限</small></span></div>
        </section>
        <section class="rail-card">
          <div class="rail-title"><span>页面信息</span><small>实时</small></div>
          <dl><div><dt>页面编码</dt><dd>{{ isSecurityAdmin ? 'security_admin' : pageCode }}</dd></div>
            <div><dt>菜单入口</dt><dd>{{ currentMenu?.name ?? '内部跳转' }}</dd></div>
            <div><dt>租户</dt><dd>{{ authState.account.tenantId }}</dd></div></dl>
        </section>
        <section class="rail-card">
          <div class="rail-title"><span>权限摘要</span><small>{{ authState.account.permissions.length }}</small></div>
          <div class="permission-list"><span v-for="permission in permissionPreview" :key="permission">{{ permission }}</span>
            <small v-if="authState.account.permissions.length > permissionPreview.length">还有 {{ authState.account.permissions.length - permissionPreview.length }} 项权限</small></div>
        </section>
      </aside>
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
.topbar { position: sticky; z-index: 100; top: 0; display: grid; grid-template-columns: 230px 1fr auto;
  align-items: center; gap: 24px; padding: 0 24px; border-bottom: 1px solid #dfe6ef; background: rgba(255,255,255,.94); backdrop-filter: blur(14px); }
.brand-block,.top-account { display: flex; align-items: center; gap: 11px; }.brand-block > span:last-child,.top-account > span:last-child { display: grid; gap: 2px; }
.brand-block b { color: #12203b; font-size: 15px; }.brand-block small,.top-account small { color: #8a98ad; font-size: 11px; }
.brand-mark { display: grid; width: 36px; height: 36px; place-items: center; border-radius: 11px; background: linear-gradient(135deg,#1d4ed8,#60a5fa); color: white; font-weight: 900; box-shadow: 0 7px 18px #2563eb32; }
.topbar-context { display: flex; align-items: center; gap: 9px; color: #7c8ba1; font-size: 13px; }.topbar-context i { color: #c5cfdb; font-style: normal; }.topbar-context strong { color: #334155; }
.environment-dot,.status-dot { width: 8px; height: 8px; border-radius: 50%; background: #22c55e; box-shadow: 0 0 0 4px #dcfce7; }
.topbar-actions { display: flex; align-items: center; gap: 12px; }.icon-button,.logout-button { border: 0; border-radius: 9px; cursor: pointer; }
.icon-button { width: 34px; height: 34px; background: #f1f5f9; color: #52627a; font-size: 18px; }.logout-button { padding: 7px 11px; background: transparent; color: #64748b; }
.mini-avatar { display: grid; width: 34px; height: 34px; place-items: center; border-radius: 10px; background: #e0e7ff; color: #3730a3; font-weight: 800; }.top-account b { color: #334155; font-size: 12px; }
.workspace { display: grid; min-width: 0; grid-template-columns: 230px minmax(520px, 1fr) 260px; }
.workspace.admin-workspace { grid-template-columns: 230px minmax(0,1fr); }
.admin-workspace .right-rail { display: none; }
.left-sidebar { position: sticky; top: 68px; display: flex; height: calc(100vh - 114px); flex-direction: column; padding: 22px 15px 16px; border-right: 1px solid #dfe6ef; background: #f8fafc; }
.sidebar-heading { display: flex; align-items: center; justify-content: space-between; padding: 0 9px 13px; color: #52627a; font-size: 12px; font-weight: 700; }.sidebar-heading small { color: #a0aec0; font-weight: 500; }
.side-menu { display: grid; gap: 6px; }.side-menu button { display: grid; grid-template-columns: 34px 1fr auto; align-items: center; gap: 10px; padding: 10px; border: 1px solid transparent; border-radius: 11px; background: transparent; color: #52627a; cursor: pointer; text-align: left; }
.side-menu button:hover { background: #eef3fa; }.side-menu button.active { border-color: #cbdcff; background: #e8f0ff; color: #1d4ed8; box-shadow: inset 3px 0 #2563eb; }
.menu-icon { display: grid; width: 32px; height: 32px; place-items: center; border-radius: 9px; background: white; box-shadow: 0 2px 7px #22345512; color: #64748b; font-size: 12px; font-weight: 800; }.active .menu-icon { background: #2563eb; color: white; }
.menu-copy { display: grid; gap: 2px; }.menu-copy b { font-size: 13px; }.menu-copy small { color: #9aa7b8; font-size: 10px; }.menu-arrow { color: #a7b2c1; font-size: 18px; }
.sidebar-foot { display: flex; align-items: center; gap: 11px; margin-top: auto; padding: 14px 12px; border: 1px solid #e3e9f1; border-radius: 12px; background: white; }.sidebar-foot > span:last-child { display: grid; gap: 2px; }.sidebar-foot b { color: #3f4d63; font-size: 12px; }.sidebar-foot small { color: #98a5b6; font-size: 10px; }
.center-stage { min-width: 0; padding: 24px; }.business-main { max-width: 1160px; margin: 0 auto; }
.hero { display: flex; min-height: 158px; align-items: center; justify-content: space-between; gap: 24px; margin-bottom: 22px; padding: 25px 28px; border: 1px solid #dde6f1; border-radius: 18px; background: linear-gradient(120deg,#ffffff 60%,#eef5ff); box-shadow: 0 10px 30px #1e3a5f0b; }
.breadcrumb { display: flex; align-items: center; gap: 8px; margin-bottom: 17px; color: #94a3b8; font-size: 11px; }.breadcrumb i { font-style: normal; }.breadcrumb strong { color: #64748b; }
.eyebrow { color: #2563eb; font-size: 10px; font-weight: 800; letter-spacing: .15em; }.hero h1 { margin: 8px 0 5px; color: #17233b; font-size: clamp(28px,3vw,38px); }.hero p { max-width: 620px; margin: 0; color: #718096; font-size: 13px; line-height: 1.7; }
.hero-side { display: grid; justify-items: center; gap: 7px; }.hero-label { color: #94a3b8; font-size: 10px; }.avatar { display: grid; width: 64px; height: 64px; place-items: center; border-radius: 19px; background: linear-gradient(135deg,#1d4ed8,#60a5fa); box-shadow: 0 10px 24px #2563eb38; color: white; font-size: 22px; font-weight: 900; }
.grid { display: grid; grid-template-columns: repeat(24,1fr); gap: var(--gap); }.grid > article { grid-column: span var(--xl); }
.page-state { padding: 70px; color: #718096; text-align: center; }.page-state.error { color: #b42318; }.page-state button { margin-left: 10px; }.page-state.empty { grid-column: 1/-1; }
.orders-placeholder { padding: 32px; border: 1px solid #e2e8f0; border-radius: 14px; background: white; box-shadow: 0 7px 22px #192b460e; }.orders-placeholder h2 { margin-top: 0; }.orders-placeholder pre { overflow: auto; padding: 16px; border-radius: 8px; background: #f1f5f9; color: #334155; }.orders-placeholder button { padding: 9px 14px; border: 0; border-radius: 8px; background: #2563eb; color: white; cursor: pointer; }
.right-rail { position: sticky; top: 68px; display: grid; height: fit-content; max-height: calc(100vh - 114px); gap: 14px; overflow: auto; padding: 24px 18px 24px 0; }.rail-card { padding: 17px; border: 1px solid #dfe6ef; border-radius: 15px; background: white; box-shadow: 0 7px 20px #2234550b; }.rail-kicker { color: #8b99ac; font-size: 10px; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
.rail-user { display: flex; align-items: center; gap: 11px; margin: 14px 0; }.rail-user > span:last-child { display: grid; gap: 3px; }.rail-user b { color: #334155; font-size: 13px; }.rail-user small { color: #94a3b8; font-size: 11px; }.large-avatar { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 12px; background: #ede9fe; color: #6d28d9; font-weight: 900; }
.rail-metrics { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }.rail-metrics span { display: grid; gap: 2px; padding: 10px; border-radius: 10px; background: #f8fafc; text-align: center; }.rail-metrics b { color: #1e40af; }.rail-metrics small { color: #94a3b8; font-size: 10px; }
.rail-title { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; color: #42516a; font-size: 12px; font-weight: 800; }.rail-title small { color: #94a3b8; font-weight: 500; }.rail-card dl { margin: 0; }.rail-card dl div { display: flex; justify-content: space-between; gap: 10px; padding: 8px 0; border-bottom: 1px dashed #e7ecf2; font-size: 11px; }.rail-card dl div:last-child { border: 0; }.rail-card dt { color: #94a3b8; }.rail-card dd { margin: 0; color: #52627a; text-align: right; }
.permission-list { display: grid; gap: 7px; }.permission-list span { overflow: hidden; padding: 7px 9px; border-radius: 8px; background: #eff6ff; color: #315caa; font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }.permission-list small { color: #94a3b8; font-size: 10px; text-align: center; }
.app-footer { display: grid; grid-template-columns: 230px 1fr auto; align-items: center; gap: 20px; padding: 0 24px; border-top: 1px solid #dfe6ef; background: #f8fafc; color: #8a98ad; font-size: 10px; }.app-footer span:nth-child(2) { text-align: center; }
.toast { position: fixed; z-index: 1100; right: 24px; bottom: 62px; padding: 12px 18px; border-radius: 9px; background: #172033; box-shadow: 0 8px 24px #0f172a33; color: white; }
@media (max-width: 1240px) { .workspace { grid-template-columns: 220px minmax(0,1fr); }.right-rail { display: none; }.topbar { grid-template-columns: 220px 1fr auto; } }
@media (max-width: 860px) { .app-shell { grid-template-rows: auto minmax(0,1fr) 46px; }.topbar { position: static; grid-template-columns: 1fr auto; min-height: 66px; }.topbar-context { display: none; }.workspace { display: block; }.left-sidebar { position: static; height: auto; padding: 10px 16px; border-right: 0; border-bottom: 1px solid #dfe6ef; }.sidebar-heading,.sidebar-foot { display: none; }.side-menu { display: flex; overflow-x: auto; }.side-menu button { min-width: 150px; grid-template-columns: 30px 1fr; }.menu-arrow { display: none; }.center-stage { padding: 18px; }.app-footer { grid-template-columns: 1fr auto; }.app-footer span:first-child { display: none; } }
@media (max-width: 600px) { .topbar { padding: 0 14px; }.brand-block small,.top-account > span:last-child,.icon-button { display: none; }.topbar-actions { gap: 5px; }.center-stage { padding: 13px; }.hero { min-height: 138px; padding: 20px; }.hero-side { display: none; }.grid > article { grid-column: span var(--xs); }.app-footer { display: flex; justify-content: center; }.app-footer span:last-child { display: none; }.page-state { padding: 45px 10px; } }
</style>
