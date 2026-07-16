package com.example.account;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AccountIdentity {
    private final String tenantId;
    private final String userId;
    private final String username;
    private final String displayName;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Set<String> features;

    public AccountIdentity(String tenantId, String userId, String username, String displayName,
                           Set<String> roles, Set<String> permissions, Set<String> features) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.roles = Collections.unmodifiableSet(new HashSet<>(roles));
        this.permissions = Collections.unmodifiableSet(new HashSet<>(permissions));
        this.features = Collections.unmodifiableSet(new HashSet<>(features));
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() { return username; }

    public String getDisplayName() { return displayName; }

    public Set<String> getRoles() { return roles; }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Set<String> getFeatures() {
        return features;
    }
}
