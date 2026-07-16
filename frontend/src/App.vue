<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getCustomerDetailCards, getHomeCards } from './api'
import DynamicCard from './components/DynamicCard.vue'
import type { CardPageDefinition } from './types'

type PageKind = 'home' | 'customer'

const params = new URLSearchParams(location.search)
const initialPage: PageKind =
  location.pathname.includes('customer-detail') || params.get('page') === 'customer'
    ? 'customer'
    : 'home'
const activePage = ref<PageKind>(initialPage)
const customerId = ref(params.get('customerId') ?? '1001')
const userId = ref('user-1')
const page = ref<CardPageDefinition | null>(null)
const error = ref('')
const loading = ref(true)

const pageContext = computed<Record<string, string>>(() =>
  activePage.value === 'customer'
    ? Object.fromEntries([['customerId', customerId.value]])
    : Object.fromEntries([['userId', userId.value]])
)
const heading = computed(() => (activePage.value === 'customer' ? '客户详情' : '工作首页'))
const subtitle = computed(() =>
  activePage.value === 'customer'
    ? `客户编号 ${customerId.value} · 卡片由后端能力、租户与权限动态决定`
    : '面向当前用户动态装配工作摘要与业务插件卡片'
)
const avatar = computed(() =>
  activePage.value === 'customer' ? customerId.value.slice(-2) : 'HI'
)

async function loadPage() {
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

function switchPage(kind: PageKind) {
  if (activePage.value === kind) {
    return
  }

  activePage.value = kind
  const url =
    kind === 'customer'
      ? `/customer-detail?customerId=${encodeURIComponent(customerId.value)}`
      : '/'
  history.pushState({}, '', url)
  loadPage()
}

onMounted(loadPage)
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
        :style="{ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }"
      />
      <div v-if="page.cards.length === 0" class="page-state empty">当前没有可用卡片</div>
    </section>
  </main>
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
