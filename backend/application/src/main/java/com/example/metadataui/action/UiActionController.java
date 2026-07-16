package com.example.metadataui.action;

import com.example.metadataui.action.model.ActionPreparation;
import com.example.metadataui.action.model.ActionResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ui/actions")
public class UiActionController {
    private final UiActionService service;

    public UiActionController(UiActionService service) {
        this.service = service;
    }

    @PostMapping("/{actionCode}/prepare")
    public ActionPreparation prepare(
            @PathVariable String actionCode,
            @RequestBody ActionPrepareRequest request,
            @RequestHeader(name = "X-Tenant-Id", defaultValue = "demo") String tenantId,
            @RequestHeader(name = "X-User-Id", defaultValue = "user-1") String userId,
            @RequestHeader(name = "X-Permissions", required = false) String permissions) {
        return service.prepare(actionCode, request, tenantId, userId, permissions);
    }

    @PostMapping("/{actionCode}/execute")
    public ActionResult execute(
            @PathVariable String actionCode,
            @RequestBody ActionExecuteRequest request,
            @RequestHeader(name = "X-Tenant-Id", defaultValue = "demo") String tenantId,
            @RequestHeader(name = "X-User-Id", defaultValue = "user-1") String userId,
            @RequestHeader(name = "X-Permissions", required = false) String permissions) {
        return service.execute(actionCode, request, tenantId, userId, permissions);
    }
}
