package com.example.metadataui.action.registry;

import com.example.metadataui.action.spi.UiActionHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class UiActionHandlerRegistry {
    private final Map<String, UiActionHandler> handlers;

    public UiActionHandlerRegistry(List<UiActionHandler> handlers) {
        Map<String, UiActionHandler> registered = new LinkedHashMap<>();
        for (UiActionHandler handler : handlers) {
            if (handler.actionCodes() == null || handler.actionCodes().isEmpty())
                throw new IllegalStateException("UI action handler actionCodes are required");
            handler.actionCodes().forEach(actionCode -> {
                if (!StringUtils.hasText(actionCode))
                    throw new IllegalStateException("UI action handler actionCode is required");
                if (registered.putIfAbsent(actionCode, handler) != null)
                    throw new IllegalStateException("duplicate UI action handler: " + actionCode);
            });
        }
        this.handlers = Map.copyOf(registered);
    }

    public Optional<UiActionHandler> find(String actionCode) {
        return Optional.ofNullable(handlers.get(actionCode));
    }
}
