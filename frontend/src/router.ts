import { createRouter, createWebHistory } from 'vue-router'

const RoutePlaceholder = { render: () => null }

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: RoutePlaceholder },
    { path: '/customer-detail', name: 'customer-detail', component: RoutePlaceholder },
    { path: '/orders', name: 'order-list', component: RoutePlaceholder }
  ]
})
