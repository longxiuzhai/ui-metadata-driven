package com.example.metadataui.card.model;

import java.util.List;

public final class ActionSuccess {
    private final String message;
    private final boolean close;
    private final List<String> refreshCards;

    public ActionSuccess(String message, boolean close, List<String> refreshCards) {
        this.message = message;
        this.close = close;
        this.refreshCards = List.copyOf(refreshCards);
    }

    public String getMessage() {
        return message;
    }

    public boolean isClose() {
        return close;
    }

    public List<String> getRefreshCards() {
        return refreshCards;
    }
}
