<script setup lang="ts">
interface Props { componentName?: string }
defineProps<Props>()
</script>
<template><div class="unknown">不支持的卡片类型：{{ componentName }}</div></template>
<style scoped>.unknown{padding:20px;color:#b45309;background:#fffbeb;border-radius:8px}</style>
