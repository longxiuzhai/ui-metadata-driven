<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { executeAction, prepareAction } from '../api'
import { formRegistry } from '../actions/formRegistry'
import type { ActionResult, OpenFormRequest } from '../types'

const props = defineProps<{
  modelValue: boolean
  request: OpenFormRequest | null
}>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: [result: ActionResult]
}>()

const values = ref<Record<string, unknown>>({})
const version = ref(0)
const formCode = ref('')
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const formComponent = computed(() => formRegistry[formCode.value])
const formTitle = computed(() => {
  if (formCode.value === 'customer_list_edit') return values.value.customerId ? '修改客户' : '新增客户'
  if (formCode.value === 'order_list_edit') return values.value.orderNo ? '修改订单' : '新增订单'
  if (formCode.value === 'customer_basic_edit') return '编辑客户基本信息'
  return '编辑表单'
})

watch(
  () => [props.modelValue, props.request] as const,
  async ([open, request]) => {
    if (!open || !request) return
    loading.value = true
    error.value = ''
    try {
      const preparation = await prepareAction(
        request.actionCode,
        request.pageCode,
        request.cardCode,
        request.params
      )
      if (!formRegistry[preparation.formCode]) {
        throw new Error(`未注册的表单：${preparation.formCode}`)
      }
      formCode.value = preparation.formCode
      values.value = { ...preparation.initialValues }
      version.value = preparation.version
    } catch (reason) {
      error.value = (reason as Error).message
    } finally {
      loading.value = false
    }
  },
  { deep: true }
)

function close() {
  if (!submitting.value) emit('update:modelValue', false)
}

async function submit() {
  const request = props.request
  if (!request || !formComponent.value) return
  submitting.value = true
  error.value = ''
  try {
    const result = await executeAction(request.actionCode, {
      pageCode: request.pageCode,
      cardCode: request.cardCode,
      params: request.params,
      values: values.value,
      version: version.value,
      requestId: globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`
    })
    emit('saved', result)
    emit('update:modelValue', false)
  } catch (reason) {
    error.value = (reason as Error).message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="overlay" @click.self="close">
      <aside class="drawer" role="dialog" aria-modal="true" :aria-label="formTitle">
        <header>
          <h2>{{ formTitle }}</h2>
          <button class="close" :disabled="submitting" @click="close">×</button>
        </header>

        <main>
          <div v-if="loading" class="state">正在加载表单…</div>
          <div v-else-if="error" class="state error">{{ error }}</div>
          <component v-else-if="formComponent" :is="formComponent" v-model="values" />
        </main>

        <footer>
          <button class="secondary" :disabled="submitting" @click="close">取消</button>
          <button :disabled="loading || submitting || !formComponent" @click="submit">
            {{ submitting ? '保存中…' : '保存' }}
          </button>
        </footer>
      </aside>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  z-index: 1000;
  inset: 0;
  display: flex;
  justify-content: flex-end;
  background: rgba(15, 23, 42, 0.42);
}

.drawer {
  display: grid;
  width: min(460px, 92vw);
  height: 100%;
  grid-template-rows: auto 1fr auto;
  background: #fff;
  box-shadow: -12px 0 32px rgba(15, 23, 42, 0.16);
}

header,
footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

header h2 {
  margin: 0;
  font-size: 18px;
}

main {
  overflow: auto;
  padding: 24px;
}

footer {
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #e2e8f0;
  border-bottom: 0;
}

button {
  padding: 9px 16px;
  border: 0;
  border-radius: 8px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.close,
.secondary {
  background: #f1f5f9;
  color: #475569;
}

.close {
  padding: 4px 10px;
  font-size: 22px;
}

.state {
  padding: 40px 0;
  color: #64748b;
  text-align: center;
}

.error {
  color: #b42318;
}
</style>
