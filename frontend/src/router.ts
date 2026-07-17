import { createRouter, createWebHistory } from 'vue-router'
import CustomerDetailView from './views/CustomerDetailView.vue'
import CustomerOrdersView from './views/CustomerOrdersView.vue'
import HomeView from './views/HomeView.vue'

const RoutePlaceholder = { render: () => null }

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView, meta: { title: '首页' } },
    { path: '/login', name: 'login', component: RoutePlaceholder },
    { path: '/register', name: 'register', component: RoutePlaceholder },
    { path: '/customer-detail', name: 'customer-detail', component: CustomerDetailView,
      meta: { title: '客户详情' } },
    { path: '/orders', name: 'order-list', component: CustomerOrdersView,
      meta: { title: '客户订单' } },
    { path: '/admin/security', name: 'security-admin', component: RoutePlaceholder,
      meta: { title: '权限管理' } }
  ]
})
