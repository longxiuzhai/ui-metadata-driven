<script setup lang="ts">
const props = defineProps<{ modelValue: Record<string, unknown> }>()
const emit = defineEmits<{ 'update:modelValue': [value: Record<string, unknown>] }>()
const update = (key: string, value: string) => emit('update:modelValue', { ...props.modelValue, [key]: value })
</script>

<template>
  <div class="form-grid">
    <label><span>订单号</span><input :value="String(modelValue.orderNo ?? '')" :disabled="Boolean(modelValue.orderNo)"
      @input="update('orderNo', ($event.target as HTMLInputElement).value)" /></label>
    <label><span>客户 ID</span><input :value="String(modelValue.customerId ?? '')"
      @input="update('customerId', ($event.target as HTMLInputElement).value)" /></label>
    <label><span>会员 ID</span><input :value="String(modelValue.memberId ?? '')"
      @input="update('memberId', ($event.target as HTMLInputElement).value)" /></label>
    <label><span>订单金额</span><input type="number" step="0.01" :value="String(modelValue.orderAmount ?? '')"
      @input="update('orderAmount', ($event.target as HTMLInputElement).value)" /></label>
    <label><span>订单状态</span><select :value="String(modelValue.orderStatus ?? 'OPEN')"
      @change="update('orderStatus', ($event.target as HTMLSelectElement).value)">
      <option value="OPEN">待处理</option><option value="PAID">已支付</option><option value="CLOSED">已关闭</option>
    </select></label>
    <label><span>下单时间（yyyy-MM-dd HH:mm）</span><input :value="String(modelValue.placedAt ?? '')"
      @input="update('placedAt', ($event.target as HTMLInputElement).value)" /></label>
  </div>
</template>

<style scoped>
.form-grid,label{display:grid;gap:14px} label{gap:7px} span{color:#475569;font-size:13px;font-weight:600}
input,select{width:100%;padding:10px 12px;border:1px solid #cbd5e1;border-radius:8px;background:#fff;color:#172033;font:inherit}
input:disabled{background:#f1f5f9;color:#64748b}
</style>
