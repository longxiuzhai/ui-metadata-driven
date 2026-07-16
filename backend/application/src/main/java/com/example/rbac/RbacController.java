package com.example.rbac;

import com.example.account.AccountIdentity;
import com.example.account.AccountService;
import com.example.rbac.entity.SysMenu;
import com.example.rbac.entity.SysPermission;
import com.example.rbac.entity.SysRole;
import com.example.rbac.service.RbacAdminService;
import com.example.rbac.service.RbacQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RbacController {
    private final RbacAdminService adminService;
    private final RbacQueryService queryService;
    private final AccountService accountService;

    public RbacController(RbacAdminService adminService, RbacQueryService queryService,
                          AccountService accountService) {
        this.adminService = adminService;
        this.queryService = queryService;
        this.accountService = accountService;
    }

    @GetMapping("/menus/me")
    public List<SysMenu> myMenus() {
        AccountIdentity account = accountService.currentIdentity()
                .orElseThrow(() -> new IllegalArgumentException("当前账号不存在"));
        return queryService.menus(Long.valueOf(account.getUserId()));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('system:user:manage')")
    public List<Map<String, Object>> users() { return adminService.users(); }

    @PutMapping("/admin/users/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:manage')")
    public Map<String, Boolean> setUserRoles(@PathVariable Long id, @RequestBody IdsRequest request) {
        adminService.setUserRoles(id, request.getIds()); return Map.of("success", true);
    }

    @PutMapping("/admin/users/{id}/status")
    @PreAuthorize("hasAuthority('system:user:manage')")
    public Map<String, Boolean> setUserStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        adminService.setUserEnabled(id, request.isEnabled()); return Map.of("success", true);
    }

    @GetMapping("/admin/roles")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public List<Map<String, Object>> roles() { return adminService.roles(); }

    @PostMapping("/admin/roles")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public SysRole createRole(@RequestBody RoleRequest request) { return saveRole(null, request); }

    @PutMapping("/admin/roles/{id}")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public SysRole updateRole(@PathVariable Long id, @RequestBody RoleRequest request) { return saveRole(id, request); }

    @DeleteMapping("/admin/roles/{id}")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public Map<String, Boolean> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id); return Map.of("success", true);
    }

    @PutMapping("/admin/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public Map<String, Boolean> setRolePermissions(@PathVariable Long id, @RequestBody IdsRequest request) {
        adminService.setRolePermissions(id, request.getIds()); return Map.of("success", true);
    }

    @PutMapping("/admin/roles/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public Map<String, Boolean> setRoleMenus(@PathVariable Long id, @RequestBody IdsRequest request) {
        adminService.setRoleMenus(id, request.getIds()); return Map.of("success", true);
    }

    @GetMapping("/admin/permissions")
    @PreAuthorize("hasAnyAuthority('system:permission:manage','system:role:manage')")
    public List<SysPermission> permissions() { return adminService.permissions(); }

    @PostMapping("/admin/permissions")
    @PreAuthorize("hasAuthority('system:permission:manage')")
    public SysPermission createPermission(@RequestBody PermissionRequest request) { return savePermission(null, request); }

    @PutMapping("/admin/permissions/{id}")
    @PreAuthorize("hasAuthority('system:permission:manage')")
    public SysPermission updatePermission(@PathVariable Long id, @RequestBody PermissionRequest request) {
        return savePermission(id, request);
    }

    @DeleteMapping("/admin/permissions/{id}")
    @PreAuthorize("hasAuthority('system:permission:manage')")
    public Map<String, Boolean> deletePermission(@PathVariable Long id) {
        adminService.deletePermission(id); return Map.of("success", true);
    }

    @GetMapping("/admin/menus")
    @PreAuthorize("hasAnyAuthority('system:menu:manage','system:role:manage')")
    public List<SysMenu> menus() { return adminService.menus(); }

    @PostMapping("/admin/menus")
    @PreAuthorize("hasAuthority('system:menu:manage')")
    public SysMenu createMenu(@RequestBody MenuRequest request) { return saveMenu(null, request); }

    @PutMapping("/admin/menus/{id}")
    @PreAuthorize("hasAuthority('system:menu:manage')")
    public SysMenu updateMenu(@PathVariable Long id, @RequestBody MenuRequest request) { return saveMenu(id, request); }

    @DeleteMapping("/admin/menus/{id}")
    @PreAuthorize("hasAuthority('system:menu:manage')")
    public Map<String, Boolean> deleteMenu(@PathVariable Long id) {
        adminService.deleteMenu(id); return Map.of("success", true);
    }

    private SysRole saveRole(Long id, RoleRequest request) {
        return adminService.saveRole(id, request.getCode(), request.getName(), request.getDescription(), request.isEnabled());
    }
    private SysPermission savePermission(Long id, PermissionRequest request) {
        return adminService.savePermission(id, request.getCode(), request.getName(), request.getResource(),
                request.getAction(), request.getDescription());
    }
    private SysMenu saveMenu(Long id, MenuRequest request) {
        return adminService.saveMenu(id, request.getParentId(), request.getCode(), request.getName(),
                request.getPath(), request.getIcon(), request.getSortOrder(),
                request.getPermissionCode(), request.isEnabled());
    }

    public static class IdsRequest {
        private List<Long> ids = List.of();
        public List<Long> getIds() { return ids; }
        public void setIds(List<Long> ids) { this.ids = ids; }
    }
    public static class StatusRequest {
        private boolean enabled;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    public static class RoleRequest {
        private String code; private String name; private String description; private boolean enabled = true;
        public String getCode() { return code; } public void setCode(String code) { this.code = code; }
        public String getName() { return name; } public void setName(String name) { this.name = name; }
        public String getDescription() { return description; } public void setDescription(String value) { description = value; }
        public boolean isEnabled() { return enabled; } public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    public static class PermissionRequest {
        private String code; private String name; private String resource; private String action; private String description;
        public String getCode() { return code; } public void setCode(String code) { this.code = code; }
        public String getName() { return name; } public void setName(String name) { this.name = name; }
        public String getResource() { return resource; } public void setResource(String value) { resource = value; }
        public String getAction() { return action; } public void setAction(String value) { action = value; }
        public String getDescription() { return description; } public void setDescription(String value) { description = value; }
    }
    public static class MenuRequest {
        private Long parentId; private String code; private String name; private String path; private String icon;
        private int sortOrder; private String permissionCode; private boolean enabled = true;
        public Long getParentId() { return parentId; } public void setParentId(Long value) { parentId = value; }
        public String getCode() { return code; } public void setCode(String value) { code = value; }
        public String getName() { return name; } public void setName(String value) { name = value; }
        public String getPath() { return path; } public void setPath(String value) { path = value; }
        public String getIcon() { return icon; } public void setIcon(String value) { icon = value; }
        public int getSortOrder() { return sortOrder; } public void setSortOrder(int value) { sortOrder = value; }
        public String getPermissionCode() { return permissionCode; } public void setPermissionCode(String value) { permissionCode = value; }
        public boolean isEnabled() { return enabled; } public void setEnabled(boolean value) { enabled = value; }
    }
}
