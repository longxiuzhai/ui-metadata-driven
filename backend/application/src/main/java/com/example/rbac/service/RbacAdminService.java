package com.example.rbac.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RbacAdminService {
    private static final Pattern CODE = Pattern.compile("^[a-zA-Z][a-zA-Z0-9:_.-]{1,99}$");
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public RbacAdminService(SysUserMapper userMapper, SysRoleMapper roleMapper,
                            SysPermissionMapper permissionMapper, SysMenuMapper menuMapper,
                            SysUserRoleMapper userRoleMapper, SysRolePermissionMapper rolePermissionMapper,
                            SysRoleMenuMapper roleMenuMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    public List<Map<String, Object>> users() {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getId)).stream()
                .map(user -> {
                    Map<String, Object> value = new LinkedHashMap<>();
                    value.put("id", user.getId()); value.put("tenantId", user.getTenantId());
                    value.put("username", user.getUsername()); value.put("displayName", user.getDisplayName());
                    value.put("email", user.getEmail()); value.put("enabled", user.getEnabled());
                    value.put("roleIds", userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                                    .eq(SysUserRole::getUserId, user.getId())).stream()
                            .map(SysUserRole::getRoleId).collect(Collectors.toList()));
                    return value;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> roles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId)).stream()
                .map(role -> {
                    Map<String, Object> value = new LinkedHashMap<>();
                    value.put("id", role.getId()); value.put("code", role.getCode()); value.put("name", role.getName());
                    value.put("description", role.getDescription()); value.put("enabled", role.getEnabled());
                    value.put("permissionIds", rolePermissionMapper.selectList(
                                    new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, role.getId()))
                            .stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList()));
                    value.put("menuIds", roleMenuMapper.selectList(
                                    new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getId()))
                            .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()));
                    return value;
                }).collect(Collectors.toList());
    }

    public List<SysPermission> permissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getCode));
    }

    public List<SysMenu> menus() {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder).orderByAsc(SysMenu::getId));
    }

    @Transactional
    public SysRole saveRole(Long id, String code, String name, String description, boolean enabled) {
        requireCode(code); requireText(name, "角色名称");
        SysRole role = id == null ? new SysRole() : requiredRole(id);
        if (id != null && Set.of("ADMIN", "USER").contains(role.getCode())
                && !role.getCode().equals(code.trim().toUpperCase())) {
            throw new IllegalArgumentException("内置 ADMIN/USER 角色编码不能修改");
        }
        if (id != null && Set.of("ADMIN", "USER").contains(role.getCode()) && !enabled) {
            throw new IllegalArgumentException("内置 ADMIN/USER 角色不能停用");
        }
        role.setCode(code.trim().toUpperCase()); role.setName(name.trim());
        role.setDescription(blankToNull(description)); role.setEnabled(enabled); role.setUpdatedAt(LocalDateTime.now());
        if (id == null) { role.setCreatedAt(LocalDateTime.now()); roleMapper.insert(role); }
        else roleMapper.updateById(role);
        return role;
    }

    @Transactional
    public void deleteRole(Long id) {
        SysRole role = requiredRole(id);
        if (Set.of("ADMIN", "USER").contains(role.getCode())) {
            throw new IllegalArgumentException("内置 ADMIN/USER 角色不能删除");
        }
        roleMapper.deleteById(id);
    }

    @Transactional
    public void setUserRoles(Long userId, List<Long> roleIds) {
        if (userMapper.selectById(userId) == null) throw new IllegalArgumentException("用户不存在");
        List<Long> ids = distinct(roleIds);
        verifyIds(ids, ids.isEmpty() ? 0 : roleMapper.selectByIds(ids).size(), "角色");
        SysRole adminRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "ADMIN"));
        boolean currentlyAdmin = adminRole != null && userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, adminRole.getId())) > 0;
        if (currentlyAdmin && !ids.contains(adminRole.getId()) && enabledAdminCount(adminRole.getId()) <= 1) {
            throw new IllegalArgumentException("不能移除最后一个启用管理员的 ADMIN 角色");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        ids.forEach(id -> userRoleMapper.insert(new SysUserRole(userId, id)));
    }

    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        if (!enabled) {
            SysRole adminRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "ADMIN"));
            if (adminRole != null && userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId).eq(SysUserRole::getRoleId, adminRole.getId())) > 0
                    && enabledAdminCount(adminRole.getId()) <= 1) {
                throw new IllegalArgumentException("不能停用最后一个启用管理员");
            }
        }
        user.setEnabled(enabled); user.setUpdatedAt(LocalDateTime.now()); userMapper.updateById(user);
    }

    @Transactional
    public void setRolePermissions(Long roleId, List<Long> permissionIds) {
        requiredRole(roleId);
        List<Long> ids = distinct(permissionIds);
        verifyIds(ids, ids.isEmpty() ? 0 : permissionMapper.selectByIds(ids).size(), "权限");
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        ids.forEach(id -> rolePermissionMapper.insert(new SysRolePermission(roleId, id)));
    }

    @Transactional
    public void setRoleMenus(Long roleId, List<Long> menuIds) {
        requiredRole(roleId);
        List<Long> ids = distinct(menuIds);
        verifyIds(ids, ids.isEmpty() ? 0 : menuMapper.selectByIds(ids).size(), "菜单");
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        ids.forEach(id -> roleMenuMapper.insert(new SysRoleMenu(roleId, id)));
    }

    @Transactional
    public SysPermission savePermission(Long id, String code, String name, String resource,
                                        String action, String description) {
        requireCode(code); requireText(name, "权限名称"); requireText(resource, "资源"); requireText(action, "动作");
        SysPermission value = id == null ? new SysPermission() : requiredPermission(id);
        value.setCode(code.trim()); value.setName(name.trim()); value.setResource(resource.trim());
        value.setActionName(action.trim()); value.setDescription(blankToNull(description));
        value.setUpdatedAt(LocalDateTime.now());
        if (id == null) { value.setCreatedAt(LocalDateTime.now()); permissionMapper.insert(value); }
        else permissionMapper.updateById(value);
        return value;
    }

    @Transactional
    public void deletePermission(Long id) {
        requiredPermission(id);
        permissionMapper.deleteById(id);
    }

    @Transactional
    public SysMenu saveMenu(Long id, Long parentId, String code, String name, String path,
                            String icon, int sortOrder, String permissionCode, boolean enabled) {
        requireCode(code); requireText(name, "菜单名称"); requireText(path, "菜单路径");
        if (!path.startsWith("/")) throw new IllegalArgumentException("菜单路径必须以 / 开头");
        if (parentId != null && menuMapper.selectById(parentId) == null) throw new IllegalArgumentException("父菜单不存在");
        SysMenu value = id == null ? new SysMenu() : requiredMenu(id);
        value.setParentId(parentId); value.setCode(code.trim()); value.setName(name.trim()); value.setPath(path.trim());
        value.setIcon(blankToNull(icon)); value.setSortOrder(sortOrder);
        value.setPermissionCode(blankToNull(permissionCode)); value.setEnabled(enabled);
        value.setUpdatedAt(LocalDateTime.now());
        if (id == null) { value.setCreatedAt(LocalDateTime.now()); menuMapper.insert(value); }
        else menuMapper.updateById(value);
        return value;
    }

    @Transactional
    public void deleteMenu(Long id) {
        requiredMenu(id);
        if (menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id)) > 0) {
            throw new IllegalArgumentException("请先删除子菜单");
        }
        menuMapper.deleteById(id);
    }

    private SysRole requiredRole(Long id) {
        SysRole value = roleMapper.selectById(id);
        if (value == null) throw new IllegalArgumentException("角色不存在");
        return value;
    }
    private SysPermission requiredPermission(Long id) {
        SysPermission value = permissionMapper.selectById(id);
        if (value == null) throw new IllegalArgumentException("权限不存在");
        return value;
    }
    private SysMenu requiredMenu(Long id) {
        SysMenu value = menuMapper.selectById(id);
        if (value == null) throw new IllegalArgumentException("菜单不存在");
        return value;
    }
    private void requireCode(String value) {
        if (value == null || !CODE.matcher(value.trim()).matches()) throw new IllegalArgumentException("编码格式不正确");
    }
    private void requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + "不能为空");
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private List<Long> distinct(List<Long> ids) {
        return ids == null ? List.of() : ids.stream().filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
    }
    private void verifyIds(List<Long> ids, int found, String label) {
        if (found != ids.size()) throw new IllegalArgumentException(label + "包含不存在的 ID");
    }

    private long enabledAdminCount(Long adminRoleId) {
        List<Long> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, adminRoleId)).stream()
                .map(SysUserRole::getUserId).distinct().collect(Collectors.toList());
        if (userIds.isEmpty()) return 0;
        return userMapper.selectByIds(userIds).stream()
                .filter(user -> Boolean.TRUE.equals(user.getEnabled())).count();
    }
}
