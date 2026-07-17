<script setup lang="ts">
import { computed } from 'vue'
import { formatters } from '../../formatters'
import type { CardAction } from '../../types'

interface Column {
  key: string
  label: string
  formatter?: string
  align?: 'left' | 'center' | 'right'
  width?: number
}

const props = defineProps<{
  data: Record<string, unknown>[]
  columns?: Column[]
  rowKey?: string
  rowActionCode?: string
  rowActionCodes?: string[]
  rowActionConfirmations?: Record<string, string>
  summaryLabel?: string
  minTableWidth?: number
  actions?: CardAction[]
}>()
const emit = defineEmits<{ action: [action: CardAction, row: Record<string, unknown>] }>()

const rowAction = computed(() =>
  props.actions?.find(action => action.code === props.rowActionCode)
)
const rowActions = computed(() =>
  (props.rowActionCodes ?? [])
    .map(code => props.actions?.find(action => action.code === code))
    .filter((action): action is CardAction => action !== undefined)
)

function show(row: Record<string, unknown>, column: Column) {
  const formatter = column.formatter ? formatters[column.formatter] : undefined
  return formatter ? formatter(row[column.key]) : String(row[column.key] ?? '-')
}

function activateRow(row: Record<string, unknown>) {
  if (rowAction.value) emit('action', rowAction.value, row)
}

function activateAction(action: CardAction, row: Record<string, unknown>) {
  const confirmation = props.rowActionConfirmations?.[action.code]
  if (confirmation && !window.confirm(confirmation)) return
  emit('action', action, row)
}
</script>

<template>
  <div class="table-card">
    <div class="table-summary">共 {{ data.length }} {{ summaryLabel ?? '条数据' }}</div>
    <div class="table-scroll">
      <table :style="{ minWidth: `${minTableWidth ?? 960}px` }">
        <thead>
          <tr>
            <th v-for="column in columns" :key="column.key"
                :style="{ width: column.width ? `${column.width}px` : undefined, textAlign: column.align ?? 'left' }">
              {{ column.label }}
            </th>
            <th v-if="rowActions.length" class="operation-column">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in data" :key="String(row[rowKey ?? 'id'] ?? index)"
              :class="{ clickable: rowAction }" :tabindex="rowAction ? 0 : undefined"
              :role="rowAction ? 'link' : undefined" :title="rowAction?.label"
              @click="activateRow(row)" @keydown.enter="activateRow(row)"
              @keydown.space.prevent="activateRow(row)">
            <td v-for="column in columns" :key="column.key"
                :style="{ textAlign: column.align ?? 'left' }">
              {{ show(row, column) }}
            </td>
            <td v-if="rowActions.length" class="operation-column">
              <button v-for="action in rowActions" :key="action.code" type="button"
                      class="row-action danger" @click.stop="activateAction(action, row)">
                {{ action.label }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.table-card { min-width: 0; }
.table-summary { margin-bottom: 10px; color: #8491a5; font-size: 12px; text-align: right; }
.table-scroll { overflow-x: auto; border: 1px solid #e6ebf2; border-radius: 10px; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }
th,td { padding: 12px 13px; border-bottom: 1px solid #edf1f5; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
th { background: #f7f9fc; color: #64748b; font-size: 12px; font-weight: 700; }
td { color: #334155; font-size: 13px; }
tbody tr:last-child td { border-bottom: 0; }
tbody tr:hover { background: #f8fbff; }
tbody tr.clickable { cursor: pointer; }
tbody tr.clickable:focus { outline: 2px solid #4f7cff; outline-offset: -2px; background: #f3f7ff; }
.operation-column { width: 90px; text-align: center; }
.row-action { padding: 5px 10px; border: 0; border-radius: 6px; cursor: pointer; font-size: 12px; }
.row-action.danger { background: #fff1f0; color: #cf1322; }
.row-action.danger:hover { background: #ffccc7; }
</style>
