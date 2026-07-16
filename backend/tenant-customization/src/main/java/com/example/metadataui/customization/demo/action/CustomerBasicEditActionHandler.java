package com.example.metadataui.customization.demo.action;

import com.example.metadataui.action.model.ActionCommand;
import com.example.metadataui.action.model.ActionContext;
import com.example.metadataui.action.model.ActionPreparation;
import com.example.metadataui.action.model.ActionResult;
import com.example.metadataui.action.spi.UiActionHandler;
import com.example.metadataui.customization.demo.DemoCustomerRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(
        name = "metadata.actions.customer-basic-edit.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CustomerBasicEditActionHandler implements UiActionHandler {
    private static final String FORM_CODE = "customer_basic_edit";

    private final DemoCustomerRepository repository;
    private final Map<String, ActionResult> completedRequests = new ConcurrentHashMap<>();

    public CustomerBasicEditActionHandler(DemoCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public String actionCode() {
        return "customer.basic.update";
    }

    @Override
    public String permission() {
        return "customer:update";
    }

    @Override
    public boolean supports(ActionContext context) {
        return "customer_detail".equals(context.getPageCode())
                && "basic_info".equals(context.getCardCode());
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
        if (completed != null) {
            return completed;
        }

        String customerId = context.requiredParam("customerId");
        String name = requiredValue(command.getValues(), "name");
        String mobile = requiredValue(command.getValues(), "mobile");
        String status = requiredValue(command.getValues(), "status");
        if (!List.of("ACTIVE", "INACTIVE").contains(status)) {
            throw new IllegalArgumentException("status must be ACTIVE or INACTIVE");
        }
        repository.update(customerId, command.getVersion(), name, mobile, status);
        ActionResult result = new ActionResult("客户信息已保存", List.of("basic_info"));
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
