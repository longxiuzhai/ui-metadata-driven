<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, useTemplateRef, watch } from 'vue'
import { loadCardData } from '../api'
import { cardRegistry } from './cards/registry'
import type { CardAction, CardDefinition } from '../types'
import UnknownCard from './cards/UnknownCard.vue'

const props = defineProps<{
  definition: CardDefinition
  context: Record<string, string>
  refreshToken?: number
}>()
const emit = defineEmits<{ action: [action: CardAction, cardData: unknown] }>()

// DynamicCard 是元数据与具体展示组件之间的适配层：
// 它负责数据加载、状态切换和动作转发，具体卡片只关心如何展示 data。
const root = useTemplateRef<HTMLElement>('root')
const data = ref<unknown>(null)
const loading = ref(false)
const error = ref('')
let controller: AbortController | undefined
let observer: IntersectionObserver | undefined

const resolvedComponent = computed(
  () => cardRegistry[props.definition.component] ?? UnknownCard
)
const isUnknown = computed(() => !cardRegistry[props.definition.component])
const isEmpty = computed(
  () => data.value == null || (Array.isArray(data.value) && data.value.length === 0)
)

async function load() {
  // 同一卡片只允许一个有效请求。刷新或卸载时取消旧请求，避免旧响应覆盖新数据。
  if (loading.value) {
    return
  }

  controller?.abort()
  controller = new AbortController()
  loading.value = true
  error.value = ''
  const started = performance.now()

  try {
    data.value = await loadCardData(props.definition.dataApi, props.context, controller.signal)
  } catch (reason) {
    if ((reason as Error).name !== 'AbortError') {
      error.value = (reason as Error).message
    }
  } finally {
    loading.value = false
    console.info('card_load', {
      card: props.definition.code,
      durationMs: Math.round(performance.now() - started),
      ok: !error.value
    })
  }
}

onMounted(() => {
  if (props.definition.loadStrategy === 'eager') {
    return load()
  }

  // on-visible 卡片接近视口 120px 时预加载，兼顾首屏速度和滚动体验。
  observer = new IntersectionObserver(
    entries => {
      if (entries.some(entry => entry.isIntersecting)) {
        observer?.disconnect()
        load()
      }
    },
    { rootMargin: '120px' }
  )
  if (root.value) {
    observer.observe(root.value)
  }
})

onBeforeUnmount(() => {
  observer?.disconnect()
  controller?.abort()
})

watch(
  () => props.refreshToken,
  (next, previous) => {
    // 父页面递增 token 即可触发指定卡片刷新，无需让动作运行时持有组件实例。
    if (previous !== undefined && next !== previous) load()
  }
)
</script>

<template>
  <article ref="root" class="card">
    <header>
      <h2>{{ definition.title }}</h2>
      <div>
        <button
          v-for="action in definition.actions"
          :key="action.code"
          @click="emit('action', action, data)"
        >
          {{ action.label }}
        </button>
      </div>
    </header>

    <div v-if="error" class="state error">
      <span>{{ error }}</span>
      <button @click="load">重试</button>
    </div>
    <div v-else-if="loading" class="skeleton">
      <i />
      <i />
      <i />
    </div>
    <div v-else-if="data === null" class="state">等待进入视口…</div>
    <div v-else-if="isEmpty" class="state">暂无数据</div>
    <component
      v-else
      :is="resolvedComponent"
      :data="data"
      v-bind="definition.props"
      :component-name="isUnknown ? definition.component : undefined"
    />
  </article>
</template>

<style scoped>
.card {
  min-height: 150px;
  padding: 22px;
  border: 1px solid #e6ebf2;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 7px 22px rgba(25, 43, 70, 0.055);
}

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  color: #172033;
  font-size: 17px;
}

button {
  padding: 7px 11px;
  border: 0;
  border-radius: 7px;
  background: #edf3ff;
  color: #2457c5;
  cursor: pointer;
}

.state {
  display: flex;
  min-height: 80px;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #8792a5;
  font-size: 14px;
}

.error {
  color: #b42318;
}

.skeleton {
  display: grid;
  gap: 12px;
}

.skeleton i {
  height: 16px;
  border-radius: 6px;
  animation: shine 1.3s infinite;
  background: linear-gradient(90deg, #edf1f6, #f8fafc, #edf1f6);
  background-size: 200%;
}

.skeleton i:nth-child(2) {
  width: 70%;
}

.skeleton i:nth-child(3) {
  width: 85%;
}

@keyframes shine {
  to {
    background-position: -200%;
  }
}
</style>
