package com.example.security;

import java.util.Set;

public final class AuthenticatedAccount {
    private final Long id;
    private final String tenantId;
    private final String username;
    private final String displayName;
    private final Set<String> roles;
    private final Set<String> permissions;

    public AuthenticatedAccount(Long id, String tenantId, String username, String displayName,
                                Set<String> roles, Set<String> permissions) {
        this.id = id;
        this.tenantId = tenantId;
        this.username = username;
        this.displayName = displayName;
        this.roles = Set.copyOf(roles);
        this.permissions = Set.copyOf(permissions);
    }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public Set<String> getRoles() { return roles; }
    public Set<String> getPermissions() { return permissions; }
}
