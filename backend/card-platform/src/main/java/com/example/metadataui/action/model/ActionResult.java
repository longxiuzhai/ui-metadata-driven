package com.example.metadataui.action.model;

import java.util.List;

public final class ActionResult {
    private final String message;
    private final List<String> refreshCards;

    public ActionResult(String message, List<String> refreshCards) {
        this.message = message;
        this.refreshCards = List.copyOf(refreshCards);
    }

    public String getMessage() {
        return message;
    }

    public List<String> getRefreshCards() {
        return refreshCards;
    }
}
