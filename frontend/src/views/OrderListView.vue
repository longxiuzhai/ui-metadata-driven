<script setup lang="ts">
import { computed } from 'vue'
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

function loadDefinition() {
  return getOrderListCards(customerId.value)
}
</script>

<template>
  <MetadataCardPage
    page-code="order_list"
    :context="pageContext"
    :load-definition="loadDefinition"
    :reload-token="reloadToken"
  />
</template>
