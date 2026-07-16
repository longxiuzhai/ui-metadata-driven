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
        name = "metadata.cards.customer-tags.enabled",
        havingValue = "true",
        matchIfMissing = true)
@CardConditional(feature = "customerTags", permissions = "customer:tag:read")
public class CustomerTagsCardProvider implements CardProvider {

    @Override
    public String pageCode() {
        return "customer_detail";
    }

    @Override
    public String cardCode() {
        return "friend_tags";
    }

    @Override
    public CardDefinition definition(CardRequestContext context) {
        return new CardDefinition(
                cardCode(), "好友标签", "TagGroupCard", 20,
                new ResponsiveSpan(24, 12, 12), "/api/customers/{customerId}/tags", "eager", "customer:tag:read",
                Map.of("labelField", "name", "colorField", "color"), List.of());
    }
}
