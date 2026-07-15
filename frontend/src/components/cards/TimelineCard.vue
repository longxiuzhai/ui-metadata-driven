<script setup lang="ts">
interface Props { data: Record<string,string>[]; timeField?: string; titleField?: string; descriptionField?: string }
defineProps<Props>()
</script>
<template><ol class="timeline"><li v-for="(item,index) in data" :key="index"><time>{{item[timeField ?? 'timestamp']}}</time><strong>{{item[titleField ?? 'eventName']}}</strong><p>{{item[descriptionField ?? 'description']}}</p></li></ol></template>
<style scoped>.timeline{list-style:none;margin:0;padding:0 0 0 12px}.timeline li{position:relative;padding:0 0 22px 22px;border-left:2px solid #dbe4f0}.timeline li:before{content:'';position:absolute;left:-6px;top:3px;width:10px;height:10px;border-radius:50%;background:#2563eb}.timeline li:last-child{padding-bottom:0}time{display:block;color:#718096;font-size:12px;margin-bottom:4px}strong{color:#172033}p{margin:5px 0 0;color:#596579;font-size:14px}</style>
