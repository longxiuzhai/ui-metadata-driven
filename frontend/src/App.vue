<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createActionRuntime } from './actions/ActionRuntime'
import { getCustomerDetailCards, getHomeCards } from './api'
import DynamicCard from './components/DynamicCard.vue'
import FormDrawer from './components/FormDrawer.vue'
import type { ActionResult, CardAction, CardPageDefinition, OpenFormRequest } from './types'

type PageKind = 'home' | 'customer' | 'orders'

const route = useRoute()
const router = useRouter()
const userId = ref('user-1')
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

watch(() => route.fullPath, loadPage, { immediate: true })
</script>

<template>
  <main>
    <nav>
      <strong>元数据 UI</strong>
      <div>
        <button :class="{ active: activePage === 'home' }" @click="switchPage('home')">
          首页
        </button>
        <button :class="{ active: activePage === 'customer' }" @click="switchPage('customer')">
          客户详情
        </button>
      </div>
    </nav>

    <section class="hero">
      <div>
        <span class="eyebrow">METADATA-DRIVEN UI · MVP</span>
        <h1>{{ heading }}</h1>
        <p>{{ subtitle }}</p>
      </div>
      <div class="avatar">{{ avatar }}</div>
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

  <FormDrawer v-model="formOpen" :request="formRequest" @saved="handleFormSaved" />
  <div v-if="toast" class="toast">{{ toast }}</div>
</template>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  background: #f4f7fb;
  color: #172033;
  font-family: Inter, "PingFang SC", "Microsoft YaHei", sans-serif;
}

main {
  max-width: 1180px;
  margin: auto;
  padding: 28px 24px 52px;
}

nav {
  display: flex;
  height: 48px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

nav strong {
  color: #1d4ed8;
}

nav div {
  display: flex;
  gap: 6px;
}

nav button {
  padding: 8px 13px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
}

nav button.active {
  background: #e8efff;
  color: #1d4ed8;
  font-weight: 700;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
}

.eyebrow {
  color: #2563eb;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.13em;
}

.hero h1 {
  margin: 9px 0 5px;
  font-size: 36px;
}

.hero p {
  margin: 0;
  color: #718096;
}

.avatar {
  display: grid;
  width: 66px;
  height: 66px;
  place-items: center;
  border-radius: 18px;
  background: linear-gradient(135deg, #1d4ed8, #60a5fa);
  box-shadow: 0 9px 22px #2563eb38;
  color: #fff;
  font-size: 24px;
  font-weight: 800;
}

.grid {
  display: grid;
  grid-template-columns: repeat(24, 1fr);
  gap: var(--gap);
}

.grid > article {
  grid-column: span var(--xl);
}

.page-state {
  padding: 70px;
  color: #718096;
  text-align: center;
}

.page-state.error {
  color: #b42318;
}

.page-state button {
  margin-left: 10px;
}

.page-state.empty {
  grid-column: 1 / -1;
}

.orders-placeholder {
  padding: 32px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 7px 22px rgba(25, 43, 70, 0.055);
}

.orders-placeholder h2 {
  margin-top: 0;
}

.orders-placeholder pre {
  overflow: auto;
  padding: 16px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #334155;
}

.orders-placeholder button {
  padding: 9px 14px;
  border: 0;
  border-radius: 8px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

.toast {
  position: fixed;
  z-index: 1100;
  right: 24px;
  bottom: 24px;
  padding: 12px 18px;
  border-radius: 9px;
  background: #172033;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.2);
  color: #fff;
}

@media (max-width: 900px) {
  .grid > article {
    grid-column: span var(--md);
  }
}

@media (max-width: 600px) {
  main {
    padding: 20px 15px 30px;
  }

  .hero h1 {
    font-size: 29px;
  }

  .avatar {
    display: none;
  }

  .grid > article {
    grid-column: span var(--xs);
  }
}
</style>
