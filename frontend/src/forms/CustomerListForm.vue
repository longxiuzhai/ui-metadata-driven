<script setup lang="ts">
const props = defineProps<{ modelValue: Record<string, unknown> }>()
const emit = defineEmits<{ 'update:modelValue': [value: Record<string, unknown>] }>()
const update = (key: string, value: string) => emit('update:modelValue', { ...props.modelValue, [key]: value })
</script>

<template>
  <div class="form-grid">
    <label><span>客户 ID</span><input :value="String(modelValue.customerId ?? '')"
      :disabled="Boolean(modelValue.customerId)" @input="update('customerId', ($event.target as HTMLInputElement).value)" /></label>
    <label><span>客户名称</span><input :value="String(modelValue.name ?? '')"
      @input="update('name', ($event.target as HTMLInputElement).value)" /></label>
    <label><span>UnionID</span><input :value="String(modelValue.unionId ?? '')"
      :disabled="Boolean(modelValue.customerId)" @input="update('unionId', ($event.target as HTMLInputElement).value)" /></label>
    <label><span>手机号</span><input :value="String(modelValue.mobile ?? '')"
      @input="update('mobile', ($event.target as HTMLInputElement).value)" /></label>
    <label><span>状态</span><select :value="String(modelValue.status ?? 'ACTIVE')"
      @change="update('status', ($event.target as HTMLSelectElement).value)">
      <option value="ACTIVE">正常</option><option value="INACTIVE">停用</option>
    </select></label>
  </div>
</template>

<style scoped>
.form-grid,label{display:grid;gap:14px} label{gap:7px} span{color:#475569;font-size:13px;font-weight:600}
input,select{width:100%;padding:10px 12px;border:1px solid #cbd5e1;border-radius:8px;background:#fff;color:#172033;font:inherit}
input:disabled{background:#f1f5f9;color:#64748b}
</style>
