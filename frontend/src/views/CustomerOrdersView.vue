<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getCustomerOrderCards } from '../api'
import MetadataCardPage from './MetadataCardPage.vue'

defineProps<{ reloadToken: number }>()

const route = useRoute()
const customerId = computed(() => String(route.query.customerId ?? '1001'))
const pageContext = computed(() => ({ customerId: customerId.value }))

function loadDefinition() {
  return getCustomerOrderCards(customerId.value)
}
</script>

<template>
  <MetadataCardPage
    page-code="customer_orders"
    :context="pageContext"
    :load-definition="loadDefinition"
    :reload-token="reloadToken"
  />
</template>
