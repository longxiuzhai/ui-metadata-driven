package com.example.metadataui.card.spi;

import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.CardDefinition;

public interface CardProvider {

    String pageCode();

    String cardCode();

    default boolean supports(CardRequestContext context) {
        return true;
    }

    CardDefinition definition(CardRequestContext context);
}
