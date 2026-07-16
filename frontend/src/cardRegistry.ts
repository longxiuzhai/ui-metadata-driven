import type { Component } from 'vue'
import KeyValueCard from './components/cards/KeyValueCard.vue'
import TagGroupCard from './components/cards/TagGroupCard.vue'
import TimelineCard from './components/cards/TimelineCard.vue'

export const cardRegistry: Record<string, Component> = {
  KeyValueCard,
  TagGroupCard,
  TimelineCard
}
