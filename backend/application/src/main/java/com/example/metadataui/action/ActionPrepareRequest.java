package com.example.metadataui.action;

import java.util.Collections;
import java.util.Map;

public class ActionPrepareRequest {
    private String pageCode;
    private String cardCode;
    private Map<String, String> params = Collections.emptyMap();

    public String getPageCode() {
        return pageCode;
    }

    public void setPageCode(String pageCode) {
        this.pageCode = pageCode;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params == null ? Collections.emptyMap() : params;
    }
}
