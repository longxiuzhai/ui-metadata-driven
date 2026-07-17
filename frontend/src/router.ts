import { createRouter, createWebHistory } from 'vue-router'
import CustomerDetailView from './views/CustomerDetailView.vue'
import CustomerListView from './views/CustomerListView.vue'
import HomeView from './views/HomeView.vue'
import OrderListView from './views/OrderListView.vue'

const RoutePlaceholder = { render: () => null }

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView, meta: { title: '首页', menuCode: 'home' } },
    { path: '/login', name: 'login', component: RoutePlaceholder },
    { path: '/register', name: 'register', component: RoutePlaceholder },
    { path: '/customers', name: 'customer-list', component: CustomerListView,
      meta: { title: '客户列表', menuCode: 'customer' } },
    { path: '/customer-detail', name: 'customer-detail', component: CustomerDetailView,
      meta: { title: '客户详情', menuCode: 'customer' } },
    { path: '/orders', name: 'order-list', component: OrderListView,
      meta: { title: '订单列表', menuCode: 'orders' } },
    { path: '/admin/security', name: 'security-admin', component: RoutePlaceholder,
      meta: { title: '权限管理', menuCode: 'security' } }
  ]
})
