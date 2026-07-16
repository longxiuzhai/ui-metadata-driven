package com.example.metadataui.card.model;

import java.util.List;
import java.util.Map;

public final class CardPageDefinition {
    private static final String VERSION = "1.0";

    private final String pageCode;
    private final Map<String, Object> layout;
    private final List<CardDefinition> cards;

    public CardPageDefinition(String pageCode, List<CardDefinition> cards) {
        this.pageCode = pageCode;
        this.layout = Map.of("type", "grid", "columns", 24, "gap", 16);
        this.cards = cards;
    }

    public String getVersion() {
        return VERSION;
    }

    public String getPageCode() {
        return pageCode;
    }

    public Map<String, Object> getLayout() {
        return layout;
    }

    public List<CardDefinition> getCards() {
        return cards;
    }
}
