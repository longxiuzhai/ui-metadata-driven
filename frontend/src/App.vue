<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getCustomerDetailCards } from './api'
import DynamicCard from './components/DynamicCard.vue'
import type { CardPageDefinition } from './types'

const customerId = ref(new URLSearchParams(location.search).get('customerId') ?? '1001')
const page = ref<CardPageDefinition | null>(null)
const error = ref('')
const loading = ref(true)
async function loadPage() {
  loading.value = true; error.value = ''
  try { page.value = await getCustomerDetailCards(customerId.value) }
  catch (e) { error.value = (e as Error).message }
  finally { loading.value = false }
}
onMounted(loadPage)
</script>

<template><main>
  <section class="hero"><div><span class="eyebrow">METADATA-DRIVEN UI · MVP</span><h1>客户详情</h1><p>客户编号 {{customerId}} · 卡片由后端能力、租户与权限动态决定</p></div><div class="avatar">{{customerId.slice(-2)}}</div></section>
  <div v-if="loading" class="page-state">正在装配客户卡片…</div>
  <div v-else-if="error" class="page-state error">{{error}} <button @click="loadPage">重试</button></div>
  <section v-else-if="page" class="grid" :style="{'--gap': `${page.layout.gap}px`}">
    <DynamicCard v-for="card in page.cards" :key="card.code" :definition="card" :customer-id="customerId"
      :style="{'--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl}" />
  </section>
</main></template>

<style>
*{box-sizing:border-box}body{margin:0;background:#f4f7fb;color:#172033;font-family:Inter,"PingFang SC","Microsoft YaHei",sans-serif}main{max-width:1180px;margin:auto;padding:52px 24px}.hero{display:flex;justify-content:space-between;align-items:center;margin-bottom:30px}.eyebrow{font-size:11px;letter-spacing:.13em;font-weight:800;color:#2563eb}.hero h1{font-size:36px;margin:9px 0 5px}.hero p{margin:0;color:#718096}.avatar{width:66px;height:66px;border-radius:18px;background:linear-gradient(135deg,#1d4ed8,#60a5fa);color:#fff;display:grid;place-items:center;font-size:24px;font-weight:800;box-shadow:0 9px 22px #2563eb38}.grid{display:grid;grid-template-columns:repeat(24,1fr);gap:var(--gap)}.grid>article{grid-column:span var(--xl)}.page-state{padding:70px;text-align:center;color:#718096}.page-state.error{color:#b42318}.page-state button{margin-left:10px}@media(max-width:900px){.grid>article{grid-column:span var(--md)}}@media(max-width:600px){main{padding:30px 15px}.hero h1{font-size:29px}.avatar{display:none}.grid>article{grid-column:span var(--xs)}}
</style>

