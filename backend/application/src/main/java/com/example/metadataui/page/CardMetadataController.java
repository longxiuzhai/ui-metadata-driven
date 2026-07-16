package com.example.metadataui.page;

import com.example.metadataui.card.model.CardPageDefinition;
import com.example.metadataui.security.CardRequestContextFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ui")
public class CardMetadataController {
    private final CardMetadataService service;
    private final CardRequestContextFactory contextFactory;

    public CardMetadataController(CardMetadataService service, CardRequestContextFactory contextFactory) {
        this.service = service;
        this.contextFactory = contextFactory;
    }

    @GetMapping("/pages/{pageCode}/cards")
    public CardPageDefinition cards(
            @PathVariable String pageCode,
            @RequestParam Map<String, String> parameters,
            @RequestHeader(name = "X-Tenant-Id", defaultValue = "demo") String tenantId,
            @RequestHeader(name = "X-User-Id", defaultValue = "user-1") String userId,
            @RequestHeader(name = "X-Permissions", required = false) String permissions,
            @RequestHeader(name = "X-Features", required = false) String features) {
        return service.getCards(contextFactory.create(pageCode, tenantId, userId, permissions, features, parameters));
    }
}
