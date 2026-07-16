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
        name = "metadata.cards.behavior-trace.enabled",
        havingValue = "true",
        matchIfMissing = true)
@CardConditional(feature = "customerBehaviorTrace", permissions = "customer:trace:read")
public class BehaviorTraceCardProvider implements CardProvider {

    @Override
    public String pageCode() {
        return "customer_detail";
    }

    @Override
    public String cardCode() {
        return "behavior_trace";
    }

    @Override
    public CardDefinition definition(CardRequestContext context) {
        return new CardDefinition(
                cardCode(), "行为轨迹", "TimelineCard", 40,
                new ResponsiveSpan(24, 24, 12), "/api/customers/{customerId}/traces",
                "on-visible", "customer:trace:read",
                Map.of(
                        "timeField", "timestamp",
                        "titleField", "eventName",
                        "descriptionField", "description"),
                List.of());
    }
}
