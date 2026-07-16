package com.example.metadataui.action.spi;

import com.example.metadataui.action.model.ActionCommand;
import com.example.metadataui.action.model.ActionContext;
import com.example.metadataui.action.model.ActionPreparation;
import com.example.metadataui.action.model.ActionResult;

public interface UiActionHandler {
    String actionCode();

    String permission();

    default boolean supports(ActionContext context) {
        return true;
    }

    ActionPreparation prepare(ActionContext context);

    ActionResult execute(ActionContext context, ActionCommand command);
}
