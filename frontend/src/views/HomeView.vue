<script setup lang="ts">
import { computed } from 'vue'
import { getHomeCards } from '../api'
import { authState } from '../auth'
import MetadataCardPage from './MetadataCardPage.vue'

defineProps<{ reloadToken: number }>()

const pageContext = computed(() => ({ userId: authState.account?.userId ?? '' }))
</script>

<template>
  <MetadataCardPage
    page-code="home"
    :context="pageContext"
    :load-definition="getHomeCards"
    :reload-token="reloadToken"
  />
</template>
