package com.example.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.rbac.entity.SysMenu;
import com.example.rbac.entity.SysPermission;
import com.example.rbac.entity.SysRole;
import com.example.rbac.entity.SysRoleMenu;
import com.example.rbac.entity.SysRolePermission;
import com.example.rbac.entity.SysUserRole;
import com.example.rbac.mapper.SysMenuMapper;
import com.example.rbac.mapper.SysPermissionMapper;
import com.example.rbac.mapper.SysRoleMapper;
import com.example.rbac.mapper.SysRoleMenuMapper;
import com.example.rbac.mapper.SysRolePermissionMapper;
import com.example.rbac.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbacQueryService {
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    public RbacQueryService(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                            SysRolePermissionMapper rolePermissionMapper, SysPermissionMapper permissionMapper,
                            SysRoleMenuMapper roleMenuMapper, SysMenuMapper menuMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
    }

    public List<Long> roleIds(Long userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)).stream()
                .map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    public List<SysRole> roles(Long userId) {
        List<Long> ids = roleIds(userId);
        if (ids.isEmpty()) return Collections.emptyList();
        return roleMapper.selectByIds(ids).stream()
                .filter(role -> Boolean.TRUE.equals(role.getEnabled()))
                .collect(Collectors.toList());
    }

    public Set<String> roleCodes(Long userId) {
        return roles(userId).stream().map(SysRole::getCode).collect(Collectors.toSet());
    }

    public Set<String> permissionCodes(Long userId) {
        List<Long> roleIds = roles(userId).stream().map(SysRole::getId).collect(Collectors.toList());
        if (roleIds.isEmpty()) return Collections.emptySet();
        List<Long> permissionIds = rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)).stream()
                .map(SysRolePermission::getPermissionId).distinct().collect(Collectors.toList());
        if (permissionIds.isEmpty()) return Collections.emptySet();
        return permissionMapper.selectByIds(permissionIds).stream()
                .map(SysPermission::getCode).collect(Collectors.toSet());
    }

    public List<SysMenu> menus(Long userId) {
        List<Long> roleIds = roles(userId).stream().map(SysRole::getId).collect(Collectors.toList());
        if (roleIds.isEmpty()) return Collections.emptyList();
        List<Long> menuIds = roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)).stream()
                .map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());
        if (menuIds.isEmpty()) return Collections.emptyList();
        Set<String> permissions = permissionCodes(userId);
        return menuMapper.selectByIds(menuIds).stream()
                .filter(menu -> Boolean.TRUE.equals(menu.getEnabled()))
                .filter(menu -> menu.getPermissionCode() == null || menu.getPermissionCode().isBlank()
                        || permissions.contains(menu.getPermissionCode()))
                .sorted((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()))
                .collect(Collectors.toList());
    }
}
