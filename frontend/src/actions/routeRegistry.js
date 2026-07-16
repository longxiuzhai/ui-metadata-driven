export const routeRegistry = {
    home: () => ({ name: 'home' }),
    customer_detail: params => ({ name: 'customer-detail', query: params }),
    order_list: params => ({ name: 'order-list', query: params })
};
