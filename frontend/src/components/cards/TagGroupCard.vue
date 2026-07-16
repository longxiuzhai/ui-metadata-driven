<script setup lang="ts">
interface Props {
  data: Record<string, string>[]
  labelField?: string
  colorField?: string
}

defineProps<Props>()
</script>

<template>
  <div class="tags">
    <span
      v-for="(item, index) in data"
      :key="index"
      :style="{
        borderColor: item[colorField ?? 'color'],
        color: item[colorField ?? 'color']
      }"
    >
      {{ item[labelField ?? 'name'] }}
    </span>
  </div>
</template>

<style scoped>
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tags span {
  padding: 7px 12px;
  border: 1px solid;
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  font-weight: 600;
}
</style>
