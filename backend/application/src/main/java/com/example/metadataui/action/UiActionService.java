package com.example.metadataui.action;

import com.example.metadataui.action.model.ActionCommand;
import com.example.metadataui.action.model.ActionContext;
import com.example.metadataui.action.model.ActionPreparation;
import com.example.metadataui.action.model.ActionResult;
import com.example.metadataui.action.registry.UiActionHandlerRegistry;
import com.example.metadataui.action.spi.UiActionHandler;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.security.CardRequestContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UiActionService {
    private static final Logger log = LoggerFactory.getLogger(UiActionService.class);

    private final UiActionHandlerRegistry handlerRegistry;
    private final CardRequestContextFactory contextFactory;

    public UiActionService(UiActionHandlerRegistry handlerRegistry, CardRequestContextFactory contextFactory) {
        this.handlerRegistry = handlerRegistry;
        this.contextFactory = contextFactory;
    }

    public ActionPreparation prepare(String actionCode, ActionPrepareRequest request,
                                     String tenantId, String userId, String permissionHeader) {
        UiActionHandler handler = handler(actionCode);
        ActionContext context = context(request, tenantId, userId, permissionHeader);
        authorize(handler, context);
        return handler.prepare(context);
    }

    public ActionResult execute(String actionCode, ActionExecuteRequest request,
                                String tenantId, String userId, String permissionHeader) {
        if (!StringUtils.hasText(request.getRequestId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestId is required");
        }
        UiActionHandler handler = handler(actionCode);
        ActionContext context = context(request, tenantId, userId, permissionHeader);
        authorize(handler, context);
        long started = System.currentTimeMillis();
        try {
            ActionResult result = handler.execute(
                    context, new ActionCommand(request.getValues(), request.getVersion(), request.getRequestId()));
            log.info("ui action: action={}, tenant={}, user={}, ok=true, durationMs={}",
                    actionCode, tenantId, userId, System.currentTimeMillis() - started);
            return result;
        } catch (RuntimeException exception) {
            log.warn("ui action: action={}, tenant={}, user={}, ok=false, durationMs={}",
                    actionCode, tenantId, userId, System.currentTimeMillis() - started);
            throw exception;
        }
    }

    private UiActionHandler handler(String actionCode) {
        return handlerRegistry.find(actionCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown actionCode"));
    }

    private ActionContext context(ActionPrepareRequest request, String tenantId,
                                  String userId, String permissionHeader) {
        if (!StringUtils.hasText(request.getPageCode()) || !StringUtils.hasText(request.getCardCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pageCode and cardCode are required");
        }
        CardRequestContext cardContext = contextFactory.create(
                request.getPageCode(), tenantId, userId, permissionHeader, null, request.getParams());
        return new ActionContext(
                tenantId, userId, request.getPageCode(), request.getCardCode(),
                cardContext.getPermissions(), request.getParams());
    }

    private void authorize(UiActionHandler handler, ActionContext context) {
        if (StringUtils.hasText(handler.permission())
                && !context.getPermissions().contains(handler.permission())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "action permission denied");
        }
        if (!handler.supports(context)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "action is not supported in this context");
        }
    }
}
