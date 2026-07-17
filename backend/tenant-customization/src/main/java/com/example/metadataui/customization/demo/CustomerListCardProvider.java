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
@CardConditional(permissions = "customer:read")
public class CustomerListCardProvider implements CardProvider, UiActionHandler {
    private static final String DELETE_ACTION_CODE = "customer.delete";

    private final DemoCustomerRepository repository;
    private final Map<String, ActionResult> completedRequests = new ConcurrentHashMap<>();

    public CustomerListCardProvider(DemoCustomerRepository repository) {
        this.repository = repository;
    }
    @Override
    public String pageCode() {
        return "customer_list";
    }

    @Override
    public String cardCode() {
        return "customer_list";
    }

    @Override
    public CardDefinition definition(CardRequestContext context) {
        CardAction openCustomer = new CardAction(
                "open_customer", "查看客户", "navigate",
                ActionTarget.route("customer_detail"), "customer:read",
                Map.of("customerId", ParameterBinding.cardData("customerId")), null);
        CardAction deleteCustomer = new CardAction(
                "delete_customer", "删除", "execute",
                ActionTarget.action(DELETE_ACTION_CODE), "customer:update",
                Map.of("customerId", ParameterBinding.cardData("customerId")), null);
        return new CardDefinition(
                cardCode(), "客户列表", "DataTableCard", 10,
                new ResponsiveSpan(24, 24, 24), "/api/customers", "eager", "customer:read",
                Map.of(
                        "rowKey", "customerId",
                        "rowActionCode", "open_customer",
                        "rowActionCodes", List.of("delete_customer"),
                        "rowActionConfirmations", Map.of("delete_customer", "确认删除该客户吗？"),
                        "summaryLabel", "位客户",
                        "minTableWidth", 900,
                        "columns", List.of(
                                Map.of("key", "customerId", "label", "客户ID", "width", 100),
                                Map.of("key", "name", "label", "客户姓名", "width", 120),
                                Map.of("key", "unionId", "label", "UnionID", "width", 190),
                                Map.of("key", "mobile", "label", "手机号", "formatter", "mobile-mask", "width", 130),
                                Map.of("key", "status", "label", "客户状态", "formatter", "customer-status", "width", 100),
                                Map.of("key", "createdAt", "label", "创建时间", "width", 160),
                                Map.of("key", "updatedAt", "label", "更新时间", "width", 160))),
                List.of(openCustomer, deleteCustomer));
    }

    @Override
    public String actionCode() {
        return DELETE_ACTION_CODE;
    }

    @Override
    public String permission() {
        return "customer:update";
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
        repository.deleteCustomer(context.requiredParam("customerId"));
        ActionResult result = new ActionResult("客户已删除", List.of(cardCode()));
        completedRequests.put(key, result);
        return result;
    }
}
