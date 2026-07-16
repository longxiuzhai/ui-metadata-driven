<script setup lang="ts">
interface Props {
  data: Record<string, string>[]
  timeField?: string
  titleField?: string
  descriptionField?: string
}

defineProps<Props>()
</script>

<template>
  <ol class="timeline">
    <li v-for="(item, index) in data" :key="index">
      <time>{{ item[timeField ?? 'timestamp'] }}</time>
      <strong>{{ item[titleField ?? 'eventName'] }}</strong>
      <p>{{ item[descriptionField ?? 'description'] }}</p>
    </li>
  </ol>
</template>

<style scoped>
.timeline {
  margin: 0;
  padding: 0 0 0 12px;
  list-style: none;
}

.timeline li {
  position: relative;
  padding: 0 0 22px 22px;
  border-left: 2px solid #dbe4f0;
}

.timeline li::before {
  position: absolute;
  top: 3px;
  left: -6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #2563eb;
  content: '';
}

.timeline li:last-child {
  padding-bottom: 0;
}

time {
  display: block;
  margin-bottom: 4px;
  color: #718096;
  font-size: 12px;
}

strong {
  color: #172033;
}

p {
  margin: 5px 0 0;
  color: #596579;
  font-size: 14px;
}
</style>
