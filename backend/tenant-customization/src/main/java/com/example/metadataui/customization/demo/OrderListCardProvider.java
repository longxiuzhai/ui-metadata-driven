package com.example.metadataui.customization.demo;

import com.example.metadataui.action.model.ActionCommand;
import com.example.metadataui.action.model.ActionContext;
import com.example.metadataui.action.model.ActionResult;
import com.example.metadataui.action.spi.UiActionHandler;
import com.example.metadataui.card.condition.CardConditional;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.ActionTarget;
import com.example.metadataui.card.model.CardAction;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.model.ParameterBinding;
import com.example.metadataui.card.model.ResponsiveSpan;
import com.example.metadataui.card.spi.CardProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@CardConditional(permissions = "order:read")
public class OrderListCardProvider implements CardProvider, UiActionHandler {
    private static final String DELETE_ACTION_CODE = "order.delete";

    private final DemoCustomerRepository repository;
    private final Map<String, ActionResult> completedRequests = new ConcurrentHashMap<>();

    public OrderListCardProvider(DemoCustomerRepository repository) {
        this.repository = repository;
    }
    @Override
    public String pageCode() {
        return "order_list";
    }

    @Override
    public String cardCode() {
        return "order_list";
    }

    @Override
    public CardDefinition definition(CardRequestContext context) {
        String customerId = context.parameter("customerId");
        boolean customerOrders = customerId != null && !customerId.isBlank();
        CardAction openCustomer = new CardAction(
                "open_customer", "查看客户", "navigate",
                ActionTarget.route("customer_detail"), "customer:read",
                Map.of("customerId", ParameterBinding.cardData("customerId")), null);
        CardAction deleteOrder = new CardAction(
                "delete_order", "删除", "execute",
                ActionTarget.action(DELETE_ACTION_CODE), "order:update",
                Map.of("orderNo", ParameterBinding.cardData("orderNo")), null);
        return new CardDefinition(
                cardCode(), customerOrders ? "客户 " + customerId + " 的订单" : "订单列表",
                "DataTableCard", 10, new ResponsiveSpan(24, 24, 24),
                customerOrders ? "/api/customers/{customerId}/orders" : "/api/orders",
                "eager", "order:read",
                Map.of(
                        "rowKey", "orderNo",
                        "rowActionCode", "open_customer",
                        "rowActionCodes", List.of("delete_order"),
                        "rowActionConfirmations", Map.of("delete_order", "确认删除该订单吗？"),
                        "summaryLabel", "笔订单",
                        "minTableWidth", 1600,
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
                List.of(openCustomer, deleteOrder));
    }

    @Override
    public String actionCode() {
        return DELETE_ACTION_CODE;
    }

    @Override
    public String permission() {
        return "order:update";
    }

    @Override
    public boolean supports(ActionContext context) {
        return pageCode().equals(context.getPageCode()) && cardCode().equals(context.getCardCode());
    }

    @Override
    public ActionResult execute(ActionContext context, ActionCommand command) {
        String key = context.getTenantId() + ":" + context.getUserId() + ":" + command.getRequestId();
        ActionResult completed = completedRequests.get(key);
        if (completed != null) return completed;
        repository.deleteOrder(context.requiredParam("orderNo"));
        ActionResult result = new ActionResult("订单已删除", List.of(cardCode()));
        completedRequests.put(key, result);
        return result;
    }
}
