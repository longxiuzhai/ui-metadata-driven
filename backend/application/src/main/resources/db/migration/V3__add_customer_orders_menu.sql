INSERT INTO sys_menu (parent_id, code, name, path, icon, sort_order, permission_code, enabled)
SELECT NULL, 'orders', '客户订单', '/orders?customerId=1001', 'receipt', 30, 'order:read', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'orders');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
JOIN sys_menu menu ON menu.code = 'orders'
WHERE role.code IN ('ADMIN', 'USER')
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu relation
      WHERE relation.role_id = role.id AND relation.menu_id = menu.id
  );
