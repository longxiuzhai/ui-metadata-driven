package com.example.metadataui.card.validation;

import com.example.metadataui.card.model.ActionTarget;
import com.example.metadataui.card.model.CardAction;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.model.ParameterBinding;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class CardDefinitionValidator {
    private static final Set<String> COMPONENTS = Set.of(
            "KeyValueCard", "TagGroupCard", "TimelineCard", "DataTableCard");
    private static final Set<String> LOAD_STRATEGIES = Set.of("eager", "on-visible");
    private static final Set<String> ACTION_TYPES = Set.of("refresh", "navigate", "open-form", "execute");
    private static final Set<String> BINDING_SOURCES = Set.of(
            "page-context", "card-data", "account", "literal");
    private static final Set<String> PRESENTATIONS = Set.of("modal", "drawer");
    private static final Pattern INTERNAL_API = Pattern.compile("^/api/[a-zA-Z0-9_/{}/.-]+$");
    private static final Pattern SAFE_PATH = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.]*$");

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
        card.getActions().forEach(this::validateAction);
    }

    private void validateAction(CardAction action) {
        require(StringUtils.hasText(action.getCode()), "action code is required");
        require(ACTION_TYPES.contains(action.getType()), "unsupported action type: " + action.getType());
        if ("navigate".equals(action.getType())) {
            require(action.getTarget() != null && StringUtils.hasText(action.getTarget().getRouteCode()),
                    "navigate action routeCode is required");
        }
        if ("open-form".equals(action.getType())) {
            ActionTarget target = action.getTarget();
            require(target != null && StringUtils.hasText(target.getFormCode()),
                    "open-form action formCode is required");
            require(target != null && StringUtils.hasText(target.getActionCode()),
                    "open-form action actionCode is required");
            require(target != null && PRESENTATIONS.contains(target.getPresentation()),
                    "open-form presentation must be modal or drawer");
        }
        if ("execute".equals(action.getType())) {
            require(action.getTarget() != null && StringUtils.hasText(action.getTarget().getActionCode()),
                    "execute action actionCode is required");
        }
        action.getParams().forEach((name, binding) -> validateBinding(name, binding));
    }

    private void validateBinding(String name, ParameterBinding binding) {
        require(SAFE_PATH.matcher(name).matches(), "invalid action parameter name: " + name);
        require(BINDING_SOURCES.contains(binding.getSource()),
                "unsupported parameter source: " + binding.getSource());
        if (!"literal".equals(binding.getSource())) {
            require(StringUtils.hasText(binding.getPath()) && SAFE_PATH.matcher(binding.getPath()).matches(),
                    "invalid parameter binding path: " + binding.getPath());
            require(!containsUnsafeSegment(binding.getPath()), "unsafe parameter binding path");
        }
    }

    private boolean containsUnsafeSegment(String path) {
        return Set.of("__proto__", "prototype", "constructor").stream()
                .anyMatch(segment -> Arrays.asList(path.split("\\.")).contains(segment));
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
