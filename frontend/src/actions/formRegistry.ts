import type { Component } from 'vue'
import CustomerBasicEditForm from '../forms/CustomerBasicEditForm.vue'

export const formRegistry: Record<string, Component> = {
  customer_basic_edit: CustomerBasicEditForm
}
