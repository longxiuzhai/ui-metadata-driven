<script setup lang="ts">
import { reactive, ref } from 'vue'
import { getCustomerListCards } from '../api'
import MetadataCardPage from './MetadataCardPage.vue'

defineProps<{ reloadToken: number }>()

interface CustomerQueryForm {
  customerId: string
  name: string
  mobile: string
  status: string
}

const queryForm = reactive<CustomerQueryForm>({ customerId: '', name: '', mobile: '', status: '' })
const requestParams = ref<Record<string, string>>({})

function query() {
  requestParams.value = {
    customerId: queryForm.customerId.trim(),
    name: queryForm.name.trim(),
    mobile: queryForm.mobile.trim(),
    status: queryForm.status
  }
}

function reset() {
  Object.assign(queryForm, { customerId: '', name: '', mobile: '', status: '' })
  requestParams.value = {}
}
</script>

<template>
  <MetadataCardPage
    page-code="customer_list"
    :context="{}"
    :request-params="requestParams"
    :load-definition="getCustomerListCards"
    :reload-token="reloadToken"
  >
    <template #before-cards>
      <form id="customer-query-form" class="business-query" @submit.prevent="query">
        <label><span>客户 ID</span><input v-model="queryForm.customerId" placeholder="精确查询客户 ID" /></label>
        <label><span>客户名称</span><input v-model="queryForm.name" placeholder="模糊查询客户名称" /></label>
        <label><span>手机号</span><input v-model="queryForm.mobile" placeholder="精确查询手机号" /></label>
        <label><span>客户状态</span><select v-model="queryForm.status">
          <option value="">全部</option><option value="ACTIVE">正常</option><option value="INACTIVE">停用</option>
        </select></label>
      </form>
    </template>
    <template #table-toolbar-after="{ cardCode }">
      <template v-if="cardCode === 'customer_list'">
        <button type="submit" form="customer-query-form" class="toolbar-query-action query">查询</button>
        <button type="button" class="toolbar-query-action reset" @click="reset">重置</button>
      </template>
    </template>
  </MetadataCardPage>
</template>

<style scoped>
.business-query{display:flex;flex-wrap:wrap;align-items:flex-end;gap:12px;padding:16px;margin-bottom:18px;border:1px solid #e6ebf2;border-radius:12px;background:#fff}
.business-query label{display:grid;min-width:160px;flex:1;gap:7px}.business-query span{color:#64748b;font-size:12px;font-weight:600}
.business-query input,.business-query select{height:36px;padding:0 11px;border:1px solid #d7dee8;border-radius:7px;background:#fff;color:#334155;font:inherit}
.toolbar-query-action{padding:7px 14px;border:0;border-radius:7px;cursor:pointer}
.toolbar-query-action.query{background:#edf3ff;color:#2457c5}.toolbar-query-action.reset{background:#e9eef5;color:#475569}
</style>
