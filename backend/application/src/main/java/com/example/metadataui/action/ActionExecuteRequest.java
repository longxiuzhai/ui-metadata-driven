package com.example.metadataui.action;

import java.util.Collections;
import java.util.Map;

public class ActionExecuteRequest extends ActionPrepareRequest {
    private Map<String, Object> values = Collections.emptyMap();
    private long version;
    private String requestId;

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values == null ? Collections.emptyMap() : values;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
