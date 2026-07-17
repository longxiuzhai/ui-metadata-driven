<script setup lang="ts">
import { formatters } from '../../formatters'

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
}>()

function show(row: Record<string, unknown>, column: Column) {
  const formatter = column.formatter ? formatters[column.formatter] : undefined
  return formatter ? formatter(row[column.key]) : String(row[column.key] ?? '-')
}
</script>

<template>
  <div class="table-card">
    <div class="table-summary">共 {{ data.length }} 条订单</div>
    <div class="table-scroll">
      <table>
        <thead>
          <tr>
            <th v-for="column in columns" :key="column.key"
                :style="{ width: column.width ? `${column.width}px` : undefined, textAlign: column.align ?? 'left' }">
              {{ column.label }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in data" :key="String(row[rowKey ?? 'id'] ?? index)">
            <td v-for="column in columns" :key="column.key"
                :style="{ textAlign: column.align ?? 'left' }">
              {{ show(row, column) }}
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
table { width: 100%; min-width: 1280px; border-collapse: collapse; table-layout: fixed; }
th,td { padding: 12px 13px; border-bottom: 1px solid #edf1f5; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
th { background: #f7f9fc; color: #64748b; font-size: 12px; font-weight: 700; }
td { color: #334155; font-size: 13px; }
tbody tr:last-child td { border-bottom: 0; }
tbody tr:hover { background: #f8fbff; }
</style>
