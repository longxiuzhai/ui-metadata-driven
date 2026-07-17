package com.example.metadataui.action.model;

import java.util.Map;
import java.util.Set;

public final class ActionContext {
    private final String tenantId;
    private final String userId;
    private final String actionCode;
    private final String pageCode;
    private final String cardCode;
    private final Set<String> permissions;
    private final Map<String, String> params;

    public ActionContext(String tenantId, String userId, String actionCode, String pageCode, String cardCode,
                         Set<String> permissions, Map<String, String> params) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.actionCode = actionCode;
        this.pageCode = pageCode;
        this.cardCode = cardCode;
        this.permissions = Set.copyOf(permissions);
        this.params = Map.copyOf(params);
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public String getActionCode() { return actionCode; }

    public String getPageCode() {
        return pageCode;
    }

    public String getCardCode() {
        return cardCode;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String requiredParam(String name) {
        String value = params.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("action parameter is required: " + name);
        }
        return value;
    }
}
