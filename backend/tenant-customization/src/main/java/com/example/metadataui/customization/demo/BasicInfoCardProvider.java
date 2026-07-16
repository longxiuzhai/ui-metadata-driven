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
        name = "metadata.cards.customer-basic.enabled",
        havingValue = "true",
        matchIfMissing = true)
@CardConditional(permissions = "customer:read")
public class BasicInfoCardProvider implements CardProvider {

    @Override
    public String pageCode() {
        return "customer_detail";
    }

    @Override
    public String cardCode() {
        return "basic_info";
    }

    @Override
    public CardDefinition definition(CardRequestContext context) {
        return new CardDefinition(
                cardCode(), "基本信息", "KeyValueCard", 10,
                new ResponsiveSpan(24, 24, 12), "/api/customers/{customerId}/basic", "eager", "customer:read",
                Map.of("columns", 2, "fields", List.of(
                        Map.of("key", "name", "label", "姓名"),
                        Map.of("key", "mobile", "label", "手机号", "formatter", "mobile-mask"),
                        Map.of("key", "status", "label", "状态", "formatter", "customer-status"))),
                List.of(new CardAction("edit", "编辑", "open-form", "customer_basic_edit", "customer:update")));
    }
}
