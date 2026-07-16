package com.example.metadataui.card.model;

public final class ActionTarget {
    private final String routeCode;
    private final String formCode;
    private final String actionCode;
    private final String presentation;

    public ActionTarget(String routeCode, String formCode, String actionCode, String presentation) {
        this.routeCode = routeCode;
        this.formCode = formCode;
        this.actionCode = actionCode;
        this.presentation = presentation;
    }

    public static ActionTarget route(String routeCode) {
        return new ActionTarget(routeCode, null, null, null);
    }

    public static ActionTarget form(String formCode, String actionCode, String presentation) {
        return new ActionTarget(null, formCode, actionCode, presentation);
    }

    public String getRouteCode() {
        return routeCode;
    }

    public String getFormCode() {
        return formCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public String getPresentation() {
        return presentation;
    }
}
