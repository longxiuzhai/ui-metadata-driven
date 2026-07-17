<script setup lang="ts">
import { getCustomerListCards } from '../api'
import MetadataCardPage from './MetadataCardPage.vue'

defineProps<{ reloadToken: number }>()
</script>

<template>
  <MetadataCardPage
    page-code="customer_list"
    :context="{}"
    :load-definition="getCustomerListCards"
    :reload-token="reloadToken"
  />
</template>
