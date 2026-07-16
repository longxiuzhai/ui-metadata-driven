package com.example.metadataui.action.model;

import java.util.Map;

public final class ActionPreparation {
    private final String formCode;
    private final Map<String, Object> initialValues;
    private final long version;

    public ActionPreparation(String formCode, Map<String, Object> initialValues, long version) {
        this.formCode = formCode;
        this.initialValues = Map.copyOf(initialValues);
        this.version = version;
    }

    public String getFormCode() {
        return formCode;
    }

    public Map<String, Object> getInitialValues() {
        return initialValues;
    }

    public long getVersion() {
        return version;
    }
}
