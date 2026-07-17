<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getCustomerDetailCards } from '../api'
import MetadataCardPage from './MetadataCardPage.vue'

defineProps<{ reloadToken: number }>()

const route = useRoute()
const customerId = computed(() => String(route.query.customerId ?? '1001'))
const pageContext = computed(() => ({ customerId: customerId.value }))

function loadDefinition() {
  return getCustomerDetailCards(customerId.value)
}
</script>

<template>
  <MetadataCardPage
    page-code="customer_detail"
    :context="pageContext"
    :load-definition="loadDefinition"
    :reload-token="reloadToken"
  />
</template>
