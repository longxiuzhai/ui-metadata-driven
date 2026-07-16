package com.example.metadataui.customization.demo;

import com.example.metadataui.card.condition.CardConditional;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.model.ResponsiveSpan;
import com.example.metadataui.card.spi.CardProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "metadata.cards.home-activity.enabled",
        havingValue = "true",
        matchIfMissing = true)
@CardConditional(feature = "homeActivity", permissions = "home:activity:read")
public class HomeActivityCardProvider implements CardProvider {

    @Override
    public String pageCode() {
        return "home";
    }

    @Override
    public String cardCode() {
        return "recent_activity";
    }

    @Override
    public CardDefinition definition(CardRequestContext context) {
        return new CardDefinition(
                cardCode(), "最近动态", "TimelineCard", 20,
                new ResponsiveSpan(24, 24, 12), "/api/home/{userId}/activities", "on-visible", "home:activity:read",
                Map.of(
                        "timeField", "timestamp",
                        "titleField", "eventName",
                        "descriptionField", "description"),
                List.of());
    }
}
