package com.example.metadataui.customization.demo;

import com.example.metadataui.action.model.ActionCommand;
import com.example.metadataui.action.model.ActionContext;
import com.example.metadataui.action.model.ActionPreparation;
import com.example.metadataui.action.model.ActionResult;
import com.example.metadataui.action.spi.UiActionHandler;
import com.example.metadataui.card.condition.CardConditional;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.ActionSuccess;
import com.example.metadataui.card.model.ActionTarget;
import com.example.metadataui.card.model.CardAction;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.model.ResponsiveSpan;
import com.example.metadataui.card.model.ParameterBinding;
import com.example.metadataui.card.spi.CardProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(
        name = "metadata.cards.customer-basic.enabled",
        havingValue = "true",
        matchIfMissing = true)
@CardConditional(permissions = "customer:read")
public class BasicInfoCardProvider implements CardProvider, UiActionHandler {
    private static final String FORM_CODE = "customer_basic_edit";
    private static final String ACTION_CODE = "customer.basic.update";

    private final DemoCustomerRepository repository;
    private final Map<String, ActionResult> completedRequests = new ConcurrentHashMap<>();

    public BasicInfoCardProvider(DemoCustomerRepository repository) {
        this.repository = repository;
    }

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
                        Map.of("key", "unionId", "label", "UnionID"),
                        Map.of("key", "mobile", "label", "手机号", "formatter", "mobile-mask"),
                        Map.of("key", "status", "label", "状态", "formatter", "customer-status"))),
                List.of(
                        new CardAction(
                                "edit", "编辑", "open-form",
                                ActionTarget.form(FORM_CODE, ACTION_CODE, "drawer"),
                                "customer:update",
                                Map.of("customerId", ParameterBinding.pageContext("customerId")),
                                new ActionSuccess("客户信息已保存", true, List.of("basic_info"))),
                        new CardAction(
                                "orders", "查看订单", "navigate",
                                ActionTarget.route("order_list"),
                                "order:read",
                                Map.of("customerId", ParameterBinding.pageContext("customerId")),
                                null)));
    }

    @Override
    public Set<String> actionCodes() {
        return Set.of(ACTION_CODE);
    }

    @Override
    public String permission() {
        return "customer:update";
    }

    @Override
    public boolean supports(ActionContext context) {
        return pageCode().equals(context.getPageCode())
                && cardCode().equals(context.getCardCode());
    }

    @Override
    public ActionPreparation prepare(ActionContext context) {
        DemoCustomerRepository.CustomerSnapshot customer = repository.get(context.requiredParam("customerId"));
        return new ActionPreparation(FORM_CODE, customer.values(), customer.getVersion());
    }

    @Override
    public ActionResult execute(ActionContext context, ActionCommand command) {
        String idempotencyKey = context.getTenantId()
                + ":" + context.getUserId()
                + ":" + command.getRequestId();
        ActionResult completed = completedRequests.get(idempotencyKey);
        if (completed != null) return completed;

        String customerId = context.requiredParam("customerId");
        String name = requiredValue(command.getValues(), "name");
        String mobile = requiredValue(command.getValues(), "mobile");
        String status = requiredValue(command.getValues(), "status");
        if (!List.of("ACTIVE", "INACTIVE").contains(status)) {
            throw new IllegalArgumentException("status must be ACTIVE or INACTIVE");
        }
        repository.update(customerId, command.getVersion(), name, mobile, status);
        ActionResult result = new ActionResult("客户信息已保存", List.of(cardCode()));
        completedRequests.put(idempotencyKey, result);
        return result;
    }

    private String requiredValue(Map<String, Object> values, String name) {
        Object value = values.get(name);
        if (!(value instanceof String) || ((String) value).isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return (String) value;
    }
}
