package com.example.metadataui.card.model;

import java.util.Map;

public final class CardAction {
    private final String code;
    private final String label;
    private final String type;
    private final ActionTarget target;
    private final String permission;
    private final Map<String, ParameterBinding> params;
    private final ActionSuccess success;

    public CardAction(String code, String label, String type, ActionTarget target, String permission,
                      Map<String, ParameterBinding> params, ActionSuccess success) {
        this.code = code;
        this.label = label;
        this.type = type;
        this.target = target;
        this.permission = permission;
        this.params = Map.copyOf(params);
        this.success = success;
    }

    public static CardAction refresh(String code, String label, String permission) {
        return new CardAction(code, label, "refresh", null, permission, Map.of(), null);
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public ActionTarget getTarget() {
        return target;
    }

    public String getPermission() {
        return permission;
    }

    public Map<String, ParameterBinding> getParams() {
        return params;
    }

    public ActionSuccess getSuccess() {
        return success;
    }
}
