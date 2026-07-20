<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderListCards } from '../api'
import MetadataCardPage from './MetadataCardPage.vue'

defineProps<{ reloadToken: number }>()

const route = useRoute()
const customerId = computed(() => {
  const value = route.query.customerId
  return value == null || value === '' ? undefined : String(value)
})
const pageContext = computed<Record<string, string>>(() => {
  const context: Record<string, string> = {}
  if (customerId.value) context.customerId = customerId.value
  return context
})
const queryForm = reactive({ orderNo: '', customerId: '', memberId: '', status: '' })
const requestParams = ref<Record<string, string>>({})

function query() {
  requestParams.value = {
    orderNo: queryForm.orderNo.trim(),
    memberId: queryForm.memberId.trim(),
    status: queryForm.status,
    ...(customerId.value ? {} : { customerId: queryForm.customerId.trim() })
  }
}

function reset() {
  Object.assign(queryForm, { orderNo: '', customerId: '', memberId: '', status: '' })
  requestParams.value = {}
}

function loadDefinition() {
  return getOrderListCards(customerId.value)
}

watch(customerId, reset)
</script>

<template>
  <MetadataCardPage
    page-code="order_list"
    :context="pageContext"
    :request-params="requestParams"
    :load-definition="loadDefinition"
    :reload-token="reloadToken"
  >
    <template #before-cards>
      <form id="order-query-form" class="business-query" @submit.prevent="query">
        <label><span>订单号</span><input v-model="queryForm.orderNo" placeholder="精确查询订单号" /></label>
        <label v-if="!customerId"><span>客户 ID</span><input v-model="queryForm.customerId" placeholder="精确查询客户 ID" /></label>
        <label><span>会员 ID</span><input v-model="queryForm.memberId" placeholder="精确查询会员 ID" /></label>
        <label><span>订单状态</span><select v-model="queryForm.status">
          <option value="">全部</option><option value="OPEN">待处理</option><option value="PAID">已支付</option><option value="CLOSED">已关闭</option>
        </select></label>
      </form>
    </template>
    <template #table-toolbar-after="{ cardCode }">
      <template v-if="cardCode === 'order_list'">
        <button type="submit" form="order-query-form" class="toolbar-query-action query">查询</button>
        <button type="button" class="toolbar-query-action reset" @click="reset">重置</button>
      </template>
    </template>
  </MetadataCardPage>
</template>

<style scoped>
.business-query{display:flex;flex-wrap:wrap;align-items:flex-end;gap:12px;padding:16px;margin-bottom:18px;border:1px solid #e6ebf2;border-radius:12px;background:#fff}
.business-query label{display:grid;min-width:170px;flex:1;gap:7px}.business-query span{color:#64748b;font-size:12px;font-weight:600}
.business-query input,.business-query select{height:36px;padding:0 11px;border:1px solid #d7dee8;border-radius:7px;background:#fff;color:#334155;font:inherit}
.toolbar-query-action{padding:7px 14px;border:0;border-radius:7px;cursor:pointer}
.toolbar-query-action.query{background:#edf3ff;color:#2457c5}.toolbar-query-action.reset{background:#e9eef5;color:#475569}
</style>
