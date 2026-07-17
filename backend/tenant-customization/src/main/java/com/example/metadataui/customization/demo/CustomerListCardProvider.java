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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@CardConditional(permissions = "customer:read")
public class CustomerListCardProvider implements CardProvider, UiActionHandler {
    private static final String FORM_CODE = "customer_list_edit";
    private static final String CREATE_ACTION_CODE = "customer.create";
    private static final String UPDATE_ACTION_CODE = "customer.list.update";
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
        CardAction viewCustomer = new CardAction(
                "view_customer", "查看", "navigate",
                ActionTarget.route("customer_detail"), "customer:read",
                Map.of("customerId", ParameterBinding.cardData("customerId")), null);
        CardAction createCustomer = new CardAction(
                "create_customer", "新增", "open-form",
                ActionTarget.form(FORM_CODE, CREATE_ACTION_CODE, "drawer"), "customer:update",
                Map.of(), null);
        CardAction editCustomer = new CardAction(
                "edit_customer", "修改", "open-form",
                ActionTarget.form(FORM_CODE, UPDATE_ACTION_CODE, "drawer"), "customer:update",
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
                        "tableActionCodes", List.of("create_customer"),
                        "rowActionCodes", List.of("view_customer", "edit_customer", "delete_customer"),
                        "rowActionConfirmations", Map.of("delete_customer", "确认删除该客户吗？"),
                        "summaryLabel", "位客户",
                        "minTableWidth", 900,
                        "columns", List.of(
                                Map.of("key", "customerId", "label", "客户ID", "width", 100, "fixed", "left"),
                                Map.of("key", "name", "label", "客户姓名", "width", 120),
                                Map.of("key", "unionId", "label", "UnionID", "width", 190),
                                Map.of("key", "mobile", "label", "手机号", "formatter", "mobile-mask", "width", 130),
                                Map.of("key", "status", "label", "客户状态", "formatter", "customer-status", "width", 100),
                                Map.of("key", "createdAt", "label", "创建时间", "width", 160),
                                Map.of("key", "updatedAt", "label", "更新时间", "width", 160))),
                List.of(createCustomer, viewCustomer, editCustomer, deleteCustomer));
    }

    @Override
    public Set<String> actionCodes() {
        return Set.of(CREATE_ACTION_CODE, UPDATE_ACTION_CODE, DELETE_ACTION_CODE);
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
    public ActionPreparation prepare(ActionContext context) {
        if (CREATE_ACTION_CODE.equals(context.getActionCode())) {
            return new ActionPreparation(FORM_CODE, Map.of("status", "ACTIVE"), 0);
        }
        DemoCustomerRepository.CustomerSnapshot customer = repository.get(context.requiredParam("customerId"));
        return new ActionPreparation(FORM_CODE, customer.values(), customer.getVersion());
    }

    @Override
    public ActionResult execute(ActionContext context, ActionCommand command) {
        String key = context.getTenantId() + ":" + context.getUserId() + ":" + command.getRequestId();
        ActionResult completed = completedRequests.get(key);
        if (completed != null) return completed;
        ActionResult result;
        if (DELETE_ACTION_CODE.equals(context.getActionCode())) {
            repository.deleteCustomer(context.requiredParam("customerId"));
            result = new ActionResult("客户已删除", List.of(cardCode()));
        } else {
            String name = required(command, "name");
            String mobile = required(command, "mobile");
            String status = required(command, "status");
            if (!List.of("ACTIVE", "INACTIVE").contains(status)) {
                throw new IllegalArgumentException("status must be ACTIVE or INACTIVE");
            }
            if (CREATE_ACTION_CODE.equals(context.getActionCode())) {
                repository.createCustomer(required(command, "customerId"), name,
                        optional(command, "unionId"), mobile, status);
                result = new ActionResult("客户已新增", List.of(cardCode()));
            } else {
                repository.update(context.requiredParam("customerId"), command.getVersion(), name, mobile, status);
                result = new ActionResult("客户已修改", List.of(cardCode()));
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
