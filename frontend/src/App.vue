<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authState, initializeAuth, logout } from './auth'
import AuthPage from './components/AuthPage.vue'
import SecurityAdmin from './components/SecurityAdmin.vue'
import MetadataCardPage from './views/MetadataCardPage.vue'

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
// 顶栏刷新只需要改变信号；具体如何重新加载由当前业务视图负责。
const pageReloadToken = ref(0)

// App 只做路由到视图的分发，不处理动态卡片内部的数据和动作状态。
const activePage = computed<PageKind>(() => {
  if (route.name === 'customer-detail') return 'customer'
  if (route.name === 'order-list') return 'orders'
  return 'home'
})
const isAuthRoute = computed(() => route.name === 'login' || route.name === 'register')
const isSecurityAdmin = computed(() => route.name === 'security-admin')
const customerId = computed(() => String(route.query.customerId ?? '1001'))
const orderContext = computed<Record<string, string>>(() =>
  Object.fromEntries(Object.entries(route.query).map(([key, value]) => [key, String(value ?? '')]))
)
const heading = computed(() => {
  if (activePage.value === 'customer') return '客户详情'
  if (activePage.value === 'orders') return '客户订单'
  return '首页'
})
const activeSecuritySection = computed<SecuritySection>(() => {
  const section = String(route.query.section ?? 'users') as SecuritySection
  return securitySections.some(item => item.key === section) ? section : 'users'
})
const activeSecurityLabel = computed(() =>
  securitySections.find(item => item.key === activeSecuritySection.value)?.label ?? '用户管理'
)

function navigateSecuritySection(section: SecuritySection) {
  router.push({ name: 'security-admin', query: { section } })
}

async function signOut() {
  await logout()
  await router.replace('/login')
}

onMounted(async () => {
  await initializeAuth()
  if (!authState.account && !isAuthRoute.value) await router.replace('/login')
  if (authState.account && isAuthRoute.value) await router.replace('/')
})

// 路由守卫只关注登录状态；业务页面加载由对应视图监听自己的 props。
watch(() => route.fullPath, async () => {
  if (authState.initialized && !authState.account && !isAuthRoute.value) {
    await router.replace('/login')
  }
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
        <button v-if="!isSecurityAdmin" class="icon-button" title="刷新当前页面"
                @click="pageReloadToken++">↻</button>
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
                    @click="router.push(menu.path)">
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
        <MetadataCardPage
          v-else-if="activePage !== 'orders'"
          :kind="activePage"
          :customer-id="customerId"
          :user-id="authState.account.userId"
          :reload-token="pageReloadToken"
        />
        <main v-else class="business-main">
          <section class="orders-placeholder">
            <h2>动态参数跳转成功</h2>
            <p>当前页面从卡片动作接收到以下查询参数：</p>
            <pre>{{ JSON.stringify(orderContext, null, 2) }}</pre>
            <button @click="router.push({ name: 'customer-detail', query: { customerId } })">返回客户详情</button>
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
</template>
