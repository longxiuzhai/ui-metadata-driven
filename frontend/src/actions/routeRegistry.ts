import type { RouteLocationRaw } from 'vue-router'

type RouteBuilder = (params: Record<string, string>) => RouteLocationRaw

export const routeRegistry: Record<string, RouteBuilder> = {
  home: () => ({ name: 'home' }),
  customer_detail: params => ({ name: 'customer-detail', query: params }),
  order_list: params => ({ name: 'order-list', query: params })
}
