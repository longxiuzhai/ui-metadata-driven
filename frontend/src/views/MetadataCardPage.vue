<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { createActionRuntime } from '../actions/ActionRuntime'
import DynamicCard from '../components/DynamicCard.vue'
import FormDrawer from '../components/FormDrawer.vue'
import type { ActionResult, CardAction, CardPageDefinition, OpenFormRequest } from '../types'

const props = defineProps<{
  pageCode: string
  context: Record<string, string>
  loadDefinition: () => Promise<CardPageDefinition>
  reloadToken: number
}>()

const router = useRouter()
const page = ref<CardPageDefinition | null>(null)
const loading = ref(true)
const error = ref('')
const refreshTokens = ref<Record<string, number>>({})
const formOpen = ref(false)
const formRequest = ref<OpenFormRequest | null>(null)
const toast = ref('')
let toastTimer: ReturnType<typeof setTimeout> | undefined
let loadSequence = 0

const contextKey = computed(() => JSON.stringify(props.context))

/**
 * 动态卡片动作统一在页面视图中落地：运行时不直接操作组件，
 * 而是通过这些回调请求局部刷新、打开表单或显示消息。
 */
const executeCardAction = createActionRuntime(router, {
  refreshCards,
  openForm(request) {
    formRequest.value = request
    formOpen.value = true
  },
  notify
})

async function loadPage() {
  const currentSequence = ++loadSequence
  loading.value = true
  error.value = ''
  page.value = null

  try {
    const result = await props.loadDefinition()
    // 快速切换路由时，丢弃较早请求的响应，避免旧页面覆盖新页面。
    if (currentSequence === loadSequence) page.value = result
  } catch (reason) {
    if (currentSequence === loadSequence) error.value = (reason as Error).message
  } finally {
    if (currentSequence === loadSequence) loading.value = false
  }
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
    pageCode: props.pageCode,
    cardCode,
    pageContext: props.context,
    cardData
  })
}

function handleFormSaved(result: ActionResult) {
  refreshCards(result.refreshCards)
  notify(result.message)
}

// 业务 View 负责定义上下文和加载函数；容器只监听通用输入。
watch(
  () => [props.pageCode, contextKey.value, props.reloadToken],
  loadPage,
  { immediate: true }
)
</script>

<template>
  <main class="business-main">
    <div v-if="loading" class="page-state">正在装配页面卡片…</div>
    <div v-else-if="error" class="page-state error">
      {{ error }}
      <button @click="loadPage">重试</button>
    </div>
    <section v-else-if="page" class="grid" :style="{ '--gap': `${page.layout.gap}px` }">
      <!-- 一项 CardDefinition 对应一个独立加载和渲染的 DynamicCard。 -->
      <DynamicCard
        v-for="card in page.cards"
        :key="card.code"
        :definition="card"
        :context="context"
        :refresh-token="refreshTokens[card.code] ?? 0"
        :style="{ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }"
        @action="(action, data) => handleCardAction(action, data, card.code)"
      />
      <div v-if="page.cards.length === 0" class="page-state empty">当前没有可用卡片</div>
    </section>
  </main>

  <FormDrawer v-model="formOpen" :request="formRequest" @saved="handleFormSaved" />
  <div v-if="toast" class="toast">{{ toast }}</div>
</template>
