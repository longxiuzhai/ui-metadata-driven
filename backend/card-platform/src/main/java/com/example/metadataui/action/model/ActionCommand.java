package com.example.metadataui.action.model;

import java.util.Map;

public final class ActionCommand {
    private final Map<String, Object> values;
    private final long version;
    private final String requestId;

    public ActionCommand(Map<String, Object> values, long version, String requestId) {
        this.values = Map.copyOf(values);
        this.version = version;
        this.requestId = requestId;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public long getVersion() {
        return version;
    }

    public String getRequestId() {
        return requestId;
    }
}
