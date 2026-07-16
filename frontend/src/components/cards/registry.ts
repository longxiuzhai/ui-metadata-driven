import type { Component } from 'vue'
import KeyValueCard from './KeyValueCard.vue'
import TagGroupCard from './TagGroupCard.vue'
import TimelineCard from './TimelineCard.vue'

/**
 * 动态卡片白名单。
 *
 * 后端元数据中的 `component` 必须与这里的 key 一致。新增卡片时需要：
 * 1. 在当前目录新增只负责展示的 Vue 组件；
 * 2. 在此处导入并注册组件；
 * 3. 让后端下发对应的 component 名称和 props。
 *
 * 使用显式注册而不是按名称动态 import，可以阻止元数据加载任意前端模块，
 * 也让可用卡片类型在一个位置即可查清。
 */
export const cardRegistry: Readonly<Record<string, Component>> = {
  KeyValueCard,
  TagGroupCard,
  TimelineCard
}
