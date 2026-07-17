import type { Component } from 'vue'
import CustomerBasicEditForm from '../forms/CustomerBasicEditForm.vue'
import CustomerListForm from '../forms/CustomerListForm.vue'
import OrderListForm from '../forms/OrderListForm.vue'

export const formRegistry: Record<string, Component> = {
  customer_basic_edit: CustomerBasicEditForm,
  customer_list_edit: CustomerListForm,
  order_list_edit: OrderListForm
}
