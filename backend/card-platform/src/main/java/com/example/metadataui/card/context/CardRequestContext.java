package com.example.metadataui.card.context;

import com.example.metadataui.card.condition.CardConditionContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class CardRequestContext implements CardConditionContext {
    private final String pageCode;
    private final String tenantId;
    private final String userId;
    private final Set<String> permissions;
    private final Map<String, Boolean> featureFlags;
    private final Map<String, String> parameters;

    public CardRequestContext(String pageCode, String tenantId, String userId, Set<String> permissions,
                              Map<String, Boolean> featureFlags, Map<String, String> parameters) {
        this.pageCode = pageCode;
        this.tenantId = tenantId;
        this.userId = userId;
        this.permissions = Collections.unmodifiableSet(new HashSet<>(permissions));
        this.featureFlags = Collections.unmodifiableMap(new HashMap<>(featureFlags));
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    public String getPageCode() {
        return pageCode;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public Map<String, Boolean> getFeatureFlags() {
        return featureFlags;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String parameter(String name) {
        return parameters.get(name);
    }
}
