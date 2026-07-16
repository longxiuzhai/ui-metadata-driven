package com.example.metadataui.customization.demo;

import com.example.metadataui.card.condition.CardConditional;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.CardAction;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.model.ResponsiveSpan;
import com.example.metadataui.card.spi.CardProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "metadata.cards.home-work-summary.enabled",
        havingValue = "true",
        matchIfMissing = true)
@CardConditional(permissions = "home:read")
public class WorkSummaryCardProvider implements CardProvider {

    @Override
    public String pageCode() {
        return "home";
    }

    @Override
    public String cardCode() {
        return "work_summary";
    }

    @Override
    public CardDefinition definition(CardRequestContext context) {
        return new CardDefinition(
                cardCode(), "今日工作", "KeyValueCard", 10,
                new ResponsiveSpan(24, 24, 12), "/api/home/{userId}/work-summary", "eager", "home:read",
                Map.of("columns", 3, "fields", List.of(
                        Map.of("key", "pending", "label", "待处理"),
                        Map.of("key", "followUps", "label", "待跟进"),
                        Map.of("key", "completed", "label", "今日完成"))),
                List.of(new CardAction("refresh", "刷新", "refresh", null, "home:read")));
    }
}
