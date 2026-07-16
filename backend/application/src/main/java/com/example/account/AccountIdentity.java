package com.example.account;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AccountIdentity {
    private final String tenantId;
    private final String userId;
    private final Set<String> permissions;
    private final Set<String> features;

    public AccountIdentity(String tenantId, String userId, Set<String> permissions, Set<String> features) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.permissions = Collections.unmodifiableSet(new HashSet<>(permissions));
        this.features = Collections.unmodifiableSet(new HashSet<>(features));
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Set<String> getFeatures() {
        return features;
    }
}
