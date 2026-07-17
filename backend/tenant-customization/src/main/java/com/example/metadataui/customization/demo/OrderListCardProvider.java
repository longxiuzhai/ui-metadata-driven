package com.example.metadataui.customization.demo;

import com.example.metadataui.action.model.ActionCommand;
import com.example.metadataui.action.model.ActionContext;
import com.example.metadataui.action.model.ActionPreparation;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@CardConditional(permissions = "order:read")
public class OrderListCardProvider implements CardProvider, UiActionHandler {
    private static final String FORM_CODE = "order_list_edit";
    private static final String CREATE_ACTION_CODE = "order.create";
    private static final String UPDATE_ACTION_CODE = "order.list.update";
    private static final String DELETE_ACTION_CODE = "order.delete";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        CardAction viewOrder = new CardAction(
                "view_order", "查看", "navigate",
                ActionTarget.route("customer_detail"), "customer:read",
                Map.of("customerId", ParameterBinding.cardData("customerId")), null);
        CardAction createOrder = new CardAction(
                "create_order", "新增", "open-form",
                ActionTarget.form(FORM_CODE, CREATE_ACTION_CODE, "drawer"), "order:update", Map.of(), null);
        CardAction editOrder = new CardAction(
                "edit_order", "修改", "open-form",
                ActionTarget.form(FORM_CODE, UPDATE_ACTION_CODE, "drawer"), "order:update",
                Map.of("orderNo", ParameterBinding.cardData("orderNo")), null);
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
                        "tableActionCodes", List.of("create_order"),
                        "rowActionCodes", List.of("view_order", "edit_order", "delete_order"),
                        "rowActionConfirmations", Map.of("delete_order", "确认删除该订单吗？"),
                        "summaryLabel", "笔订单",
                        "minTableWidth", 1600,
                        "columns", List.of(
                                Map.of("key", "orderNo", "label", "订单号", "width", 150, "fixed", "left"),
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
                List.of(createOrder, viewOrder, editOrder, deleteOrder));
    }

    @Override
    public Set<String> actionCodes() {
        return Set.of(CREATE_ACTION_CODE, UPDATE_ACTION_CODE, DELETE_ACTION_CODE);
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
    public ActionPreparation prepare(ActionContext context) {
        if (CREATE_ACTION_CODE.equals(context.getActionCode())) {
            return new ActionPreparation(FORM_CODE,
                    Map.of("orderStatus", "OPEN", "placedAt", DATE_TIME.format(LocalDateTime.now())), 0);
        }
        return new ActionPreparation(FORM_CODE, repository.order(context.requiredParam("orderNo")), 0);
    }

    @Override
    public ActionResult execute(ActionContext context, ActionCommand command) {
        String key = context.getTenantId() + ":" + context.getUserId() + ":" + command.getRequestId();
        ActionResult completed = completedRequests.get(key);
        if (completed != null) return completed;
        ActionResult result;
        if (DELETE_ACTION_CODE.equals(context.getActionCode())) {
            repository.deleteOrder(context.requiredParam("orderNo"));
            result = new ActionResult("订单已删除", List.of(cardCode()));
        } else {
            String customerId = required(command, "customerId");
            String memberId = optional(command, "memberId");
            BigDecimal amount = new BigDecimal(required(command, "orderAmount"));
            String status = required(command, "orderStatus");
            if (!List.of("OPEN", "PAID", "CLOSED").contains(status)) {
                throw new IllegalArgumentException("orderStatus must be OPEN, PAID or CLOSED");
            }
            LocalDateTime placedAt = LocalDateTime.parse(required(command, "placedAt"), DATE_TIME);
            if (CREATE_ACTION_CODE.equals(context.getActionCode())) {
                repository.createOrder(required(command, "orderNo"), customerId, memberId, amount, status, placedAt);
                result = new ActionResult("订单已新增", List.of(cardCode()));
            } else {
                repository.updateOrder(context.requiredParam("orderNo"), customerId, memberId, amount, status, placedAt);
                result = new ActionResult("订单已修改", List.of(cardCode()));
            }
        }
        completedRequests.put(key, result);
        return result;
    }

    private String required(ActionCommand command, String name) {
        String value = optional(command, name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }

    private String optional(ActionCommand command, String name) {
        Object value = command.getValues().get(name);
        return value == null ? null : String.valueOf(value).trim();
    }
}
