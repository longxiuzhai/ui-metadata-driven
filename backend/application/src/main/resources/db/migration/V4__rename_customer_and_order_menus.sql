UPDATE sys_menu
SET name = '客户列表', path = '/customers', updated_at = CURRENT_TIMESTAMP
WHERE code = 'customer';

UPDATE sys_menu
SET name = '订单列表', path = '/orders', updated_at = CURRENT_TIMESTAMP
WHERE code = 'orders';
