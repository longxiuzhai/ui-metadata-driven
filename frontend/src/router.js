import { createRouter, createWebHistory } from 'vue-router';
const RoutePlaceholder = { render: () => null };
export const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', name: 'home', component: RoutePlaceholder },
        { path: '/login', name: 'login', component: RoutePlaceholder },
        { path: '/register', name: 'register', component: RoutePlaceholder },
        { path: '/customer-detail', name: 'customer-detail', component: RoutePlaceholder },
        { path: '/orders', name: 'order-list', component: RoutePlaceholder },
        { path: '/admin/security', name: 'security-admin', component: RoutePlaceholder }
    ]
});
