package com.example.rbac.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.rbac.entity.SysMenu;
import com.example.rbac.entity.SysPermission;
import com.example.rbac.entity.SysRole;
import com.example.rbac.entity.SysRoleMenu;
import com.example.rbac.entity.SysRolePermission;
import com.example.rbac.entity.SysUser;
import com.example.rbac.entity.SysUserRole;
import com.example.rbac.mapper.SysMenuMapper;
import com.example.rbac.mapper.SysPermissionMapper;
import com.example.rbac.mapper.SysRoleMapper;
import com.example.rbac.mapper.SysRoleMenuMapper;
import com.example.rbac.mapper.SysRolePermissionMapper;
import com.example.rbac.mapper.SysUserMapper;
import com.example.rbac.mapper.SysUserRoleMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RbacBootstrap implements ApplicationRunner {
    private final BootstrapProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public RbacBootstrap(BootstrapProperties properties, PasswordEncoder passwordEncoder,
                         SysUserMapper userMapper, SysRoleMapper roleMapper,
                         SysPermissionMapper permissionMapper, SysMenuMapper menuMapper,
                         SysUserRoleMapper userRoleMapper, SysRolePermissionMapper rolePermissionMapper,
                         SysRoleMenuMapper roleMenuMapper) {
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) return;
        List<SysPermission> permissions = new ArrayList<>();
        permissions.add(permission("home:read", "查看首页", "home", "read"));
        permissions.add(permission("home:activity:read", "查看首页动态", "home.activity", "read"));
        permissions.add(permission("customer:read", "查看客户", "customer", "read"));
        permissions.add(permission("customer:update", "编辑客户", "customer", "update"));
        permissions.add(permission("customer:tag:read", "查看客户标签", "customer.tag", "read"));
        permissions.add(permission("customer:trace:read", "查看客户轨迹", "customer.trace", "read"));
        permissions.add(permission("order:read", "查看订单", "order", "read"));
        permissions.add(permission("order:update", "编辑订单", "order", "update"));
        permissions.add(permission("system:user:manage", "用户管理", "system.user", "manage"));
        permissions.add(permission("system:role:manage", "角色管理", "system.role", "manage"));
        permissions.add(permission("system:permission:manage", "权限管理", "system.permission", "manage"));
        permissions.add(permission("system:menu:manage", "菜单管理", "system.menu", "manage"));

        SysMenu home = menu("home", "首页", "/", "home", 10, "home:read");
        SysMenu customer = menu("customer", "客户列表", "/customers",
                "users", 20, "customer:read");
        SysMenu orders = menu("orders", "订单列表", "/orders",
                "receipt", 30, "order:read");
        SysMenu security = menu("security", "权限管理", "/admin/security",
                "shield", 90, "system:role:manage");

        SysRole userRole = role("USER", "普通用户", "注册用户默认角色");
        SysRole adminRole = role("ADMIN", "系统管理员", "拥有全部系统权限");
        assignRolePermissionsIfEmpty(userRole.getId(), permissions.stream()
                .filter(value -> !value.getCode().startsWith("system:"))
                .map(SysPermission::getId).collect(java.util.stream.Collectors.toList()));
        assignRoleMenusIfEmpty(userRole.getId(), List.of(home.getId(), customer.getId(), orders.getId()));
        assignRolePermissionsIfEmpty(adminRole.getId(), permissions.stream()
                .map(SysPermission::getId).collect(java.util.stream.Collectors.toList()));
        assignRoleMenusIfEmpty(adminRole.getId(), List.of(home.getId(), customer.getId(), orders.getId(), security.getId()));

        String adminUsername = properties.getAdminUsername().trim().toLowerCase();
        SysUser admin = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, "demo").eq(SysUser::getUsername, adminUsername));
        if (admin == null) {
            LocalDateTime now = LocalDateTime.now();
            admin = new SysUser();
            admin.setTenantId("demo");
            admin.setUsername(adminUsername);
            admin.setPasswordHash(passwordEncoder.encode(properties.getAdminPassword()));
            admin.setDisplayName("系统管理员");
            admin.setEmail(null);
            admin.setEnabled(true);
            admin.setCreatedAt(now);
            admin.setUpdatedAt(now);
            userMapper.insert(admin);
        }
        if (userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, admin.getId()).eq(SysUserRole::getRoleId, adminRole.getId())) == 0) {
            userRoleMapper.insert(new SysUserRole(admin.getId(), adminRole.getId()));
        }
    }

    private SysPermission permission(String code, String name, String resource, String action) {
        SysPermission value = permissionMapper.selectOne(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getCode, code));
        if (value != null) return value;
        value = new SysPermission();
        value.setCode(code); value.setName(name); value.setResource(resource); value.setActionName(action);
        value.setDescription(null); value.setCreatedAt(LocalDateTime.now()); value.setUpdatedAt(LocalDateTime.now());
        permissionMapper.insert(value);
        return value;
    }

    private SysRole role(String code, String name, String description) {
        SysRole value = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code));
        if (value != null) return value;
        value = new SysRole();
        value.setCode(code); value.setName(name); value.setDescription(description); value.setEnabled(true);
        value.setCreatedAt(LocalDateTime.now()); value.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(value);
        return value;
    }

    private SysMenu menu(String code, String name, String path, String icon, int order, String permission) {
        SysMenu value = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getCode, code));
        if (value != null) return value;
        value = new SysMenu();
        value.setParentId(null); value.setCode(code); value.setName(name); value.setPath(path); value.setIcon(icon);
        value.setSortOrder(order); value.setPermissionCode(permission); value.setEnabled(true);
        value.setCreatedAt(LocalDateTime.now()); value.setUpdatedAt(LocalDateTime.now());
        menuMapper.insert(value);
        return value;
    }

    private void assignRolePermissionsIfEmpty(Long roleId, List<Long> ids) {
        if (rolePermissionMapper.selectCount(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId)) == 0) {
            ids.forEach(id -> rolePermissionMapper.insert(new SysRolePermission(roleId, id)));
        }
    }

    private void assignRoleMenusIfEmpty(Long roleId, List<Long> ids) {
        if (roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)) == 0) {
            ids.forEach(id -> roleMenuMapper.insert(new SysRoleMenu(roleId, id)));
        }
    }
}
