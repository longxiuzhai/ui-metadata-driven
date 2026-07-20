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
  fixed?: 'left' | 'right'
}

const props = withDefaults(defineProps<{
  data: Record<string, unknown>[]
  columns?: Column[]
  rowKey?: string
  rowActionCode?: string
  rowActionCodes?: string[]
  tableActionCodes?: string[]
  rowActionConfirmations?: Record<string, string>
  operationFixed?: boolean
  summaryLabel?: string
  minTableWidth?: number
  actions?: CardAction[]
}>(), {
  operationFixed: true
})
const emit = defineEmits<{ action: [action: CardAction, row: Record<string, unknown>] }>()

const rowAction = computed(() =>
  props.actions?.find(action => action.code === props.rowActionCode)
)
const rowActions = computed(() =>
  (props.rowActionCodes ?? [])
    .map(code => props.actions?.find(action => action.code === code))
    .filter((action): action is CardAction => action !== undefined)
)
const tableActions = computed(() =>
  (props.tableActionCodes ?? [])
    .map(code => props.actions?.find(action => action.code === code))
    .filter((action): action is CardAction => action !== undefined)
)
const operationWidth = computed(() => Math.max(90, rowActions.value.length * 58 + 24))
const operationColumnStyle = computed(() => ({ width: `${operationWidth.value}px` }))

function columnStyle(column: Column, index: number) {
  const style: Record<string, string> = {
    width: column.width ? `${column.width}px` : '',
    textAlign: column.align ?? 'left'
  }
  if (column.fixed === 'left') {
    style.left = `${(props.columns ?? []).slice(0, index)
      .filter(item => item.fixed === 'left').reduce((sum, item) => sum + (item.width ?? 120), 0)}px`
  }
  if (column.fixed === 'right') {
    const following = (props.columns ?? []).slice(index + 1)
      .filter(item => item.fixed === 'right').reduce((sum, item) => sum + (item.width ?? 120), 0)
    style.right = `${following + (rowActions.value.length && props.operationFixed !== false ? operationWidth.value : 0)}px`
  }
  return style
}

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
    <div class="table-toolbar">
      <div class="table-summary">共 {{ data.length }} {{ summaryLabel ?? '条数据' }}</div>
      <div class="table-actions">
        <button v-for="action in tableActions" :key="action.code" type="button"
                class="toolbar-action" @click="emit('action', action, {})">{{ action.label }}</button>
        <slot name="toolbar-after" />
      </div>
    </div>
    <div class="table-scroll">
      <table :style="{ minWidth: `${minTableWidth ?? 960}px` }">
        <thead>
          <tr>
            <th v-for="(column, index) in columns" :key="column.key"
                :class="column.fixed ? `fixed-${column.fixed}` : undefined" :style="columnStyle(column, index)">
              {{ column.label }}
            </th>
            <th v-if="rowActions.length" class="operation-column"
                :class="{ 'fixed-right': operationFixed !== false }" :style="operationColumnStyle">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in data" :key="String(row[rowKey ?? 'id'] ?? index)"
              :class="{ clickable: rowAction }" :tabindex="rowAction ? 0 : undefined"
              :role="rowAction ? 'link' : undefined" :title="rowAction?.label"
              @click="activateRow(row)" @keydown.enter="activateRow(row)"
              @keydown.space.prevent="activateRow(row)">
            <td v-for="(column, columnIndex) in columns" :key="column.key"
                :class="column.fixed ? `fixed-${column.fixed}` : undefined"
                :style="columnStyle(column, columnIndex)">
              {{ show(row, column) }}
            </td>
            <td v-if="rowActions.length" class="operation-column"
                :class="{ 'fixed-right': operationFixed !== false }" :style="operationColumnStyle">
              <button v-for="action in rowActions" :key="action.code" type="button"
                      class="row-action" :class="{ danger: action.code.includes('delete') }"
                      @click.stop="activateAction(action, row)">
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
.table-toolbar { display: flex; min-height: 32px; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.table-actions { display: flex; gap: 8px; }
.table-summary { color: #8491a5; font-size: 12px; text-align: left; }
.toolbar-action { padding: 7px 14px; border: 0; border-radius: 7px; background: #2563eb; color: #fff; cursor: pointer; }
.table-scroll { overflow-x: auto; border: 1px solid #e6ebf2; border-radius: 10px; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }
th,td { padding: 12px 13px; border-bottom: 1px solid #edf1f5; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
th { background: #f7f9fc; color: #64748b; font-size: 12px; font-weight: 700; }
td { color: #334155; font-size: 13px; }
.fixed-left,.fixed-right { position: sticky; z-index: 2; background: #fff; }
th.fixed-left,th.fixed-right { z-index: 3; background: #f7f9fc; }
.fixed-left { box-shadow: 2px 0 4px rgba(15, 23, 42, 0.06); }
.fixed-right { right: 0; box-shadow: -2px 0 4px rgba(15, 23, 42, 0.08); }
tbody tr:last-child td { border-bottom: 0; }
tbody tr:hover { background: #f8fbff; }
tbody tr.clickable { cursor: pointer; }
tbody tr.clickable:focus { outline: 2px solid #4f7cff; outline-offset: -2px; background: #f3f7ff; }
.operation-column { text-align: center; }
.operation-column .row-action + .row-action { margin-left: 6px; }
.row-action { padding: 5px 10px; border: 0; border-radius: 6px; cursor: pointer; font-size: 12px; }
.row-action.danger { background: #fff1f0; color: #cf1322; }
.row-action.danger:hover { background: #ffccc7; }
</style>
