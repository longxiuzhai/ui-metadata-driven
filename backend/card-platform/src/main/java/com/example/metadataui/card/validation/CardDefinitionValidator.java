package com.example.metadataui.card.validation;

import com.example.metadataui.card.model.CardDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class CardDefinitionValidator {
    private static final Set<String> COMPONENTS = Set.of("KeyValueCard", "TagGroupCard", "TimelineCard");
    private static final Set<String> LOAD_STRATEGIES = Set.of("eager", "on-visible");
    private static final Pattern INTERNAL_API = Pattern.compile("^/api/[a-zA-Z0-9_/{}/.-]+$");

    public void validate(CardDefinition card) {
        require(StringUtils.hasText(card.getCode()), "card code is required");
        require(COMPONENTS.contains(card.getComponent()), "unsupported component: " + card.getComponent());
        require(LOAD_STRATEGIES.contains(card.getLoadStrategy()),
                "unsupported load strategy: " + card.getLoadStrategy());
        require(INTERNAL_API.matcher(card.getDataApi()).matches(), "dataApi must be an internal /api path");
        require(card.getSpan() != null
                        && isValidSpan(card.getSpan().getXs())
                        && isValidSpan(card.getSpan().getMd())
                        && isValidSpan(card.getSpan().getXl()),
                "span must be 1..24");
    }

    private boolean isValidSpan(int span) {
        return span >= 1 && span <= 24;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
