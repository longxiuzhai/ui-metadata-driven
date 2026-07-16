<script setup lang="ts">
import { formatters } from '../../formatters'

interface Field {
  key: string
  label: string
  formatter?: string
}

const props = defineProps<{
  data: Record<string, unknown>
  fields?: Field[]
  columns?: number
}>()

function show(field: Field) {
  const formatter = field.formatter ? formatters[field.formatter] : undefined
  return formatter ? formatter(props.data[field.key]) : String(props.data[field.key] ?? '-')
}
</script>

<template>
  <dl class="kv" :style="{ '--columns': columns ?? 2 }">
    <div v-for="field in fields" :key="field.key">
      <dt>{{ field.label }}</dt>
      <dd>{{ show(field) }}</dd>
    </div>
  </dl>
</template>

<style scoped>
.kv {
  display: grid;
  grid-template-columns: repeat(var(--columns), 1fr);
  gap: 18px;
}

.kv div {
  min-width: 0;
}

dt {
  margin-bottom: 6px;
  color: #718096;
  font-size: 13px;
}

dd {
  margin: 0;
  color: #172033;
  font-weight: 600;
}
</style>
