package com.example.metadataui.customization.demo;

import com.example.metadataui.card.condition.CardConditional;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.ActionTarget;
import com.example.metadataui.card.model.CardAction;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.model.ParameterBinding;
import com.example.metadataui.card.model.ResponsiveSpan;
import com.example.metadataui.card.spi.CardProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "metadata.cards.customer-orders.enabled",
        havingValue = "true",
        matchIfMissing = true)
@CardConditional(permissions = "order:read")
public class CustomerOrdersCardProvider implements CardProvider {
    @Override
    public String pageCode() {
        return "customer_orders";
    }

    @Override
    public String cardCode() {
        return "customer_order_list";
    }

    @Override
    public CardDefinition definition(CardRequestContext context) {
        return new CardDefinition(
                cardCode(), "订单列表", "DataTableCard", 10,
                new ResponsiveSpan(24, 24, 24),
                "/api/customers/{customerId}/orders", "eager", "order:read",
                Map.of(
                        "rowKey", "orderNo",
                        "columns", List.of(
                                Map.of("key", "orderNo", "label", "订单号", "width", 150),
                                Map.of("key", "customerId", "label", "客户ID", "width", 90),
                                Map.of("key", "memberId", "label", "会员ID", "width", 100),
                                Map.of("key", "memberUnionId", "label", "会员UnionID", "width", 180),
                                Map.of("key", "memberMobile", "label", "会员手机号", "formatter", "mobile-mask", "width", 120),
                                Map.of("key", "memberLevel", "label", "会员等级", "formatter", "member-level", "width", 90),
                                Map.of("key", "memberStatus", "label", "会员状态", "formatter", "customer-status", "width", 90),
                                Map.of("key", "orderAmount", "label", "订单金额", "formatter", "money-cny", "align", "right", "width", 110),
                                Map.of("key", "orderStatus", "label", "订单状态", "formatter", "order-status", "width", 90),
                                Map.of("key", "placedAt", "label", "下单时间", "width", 150),
                                Map.of("key", "createdAt", "label", "创建时间", "width", 150),
                                Map.of("key", "updatedAt", "label", "更新时间", "width", 150))),
                List.of(new CardAction(
                        "back_customer", "返回客户详情", "navigate",
                        ActionTarget.route("customer_detail"), "customer:read",
                        Map.of("customerId", ParameterBinding.pageContext("customerId")), null)));
    }
}
