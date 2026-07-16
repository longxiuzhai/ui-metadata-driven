package com.example.account;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public AccountIdentity me(
            @RequestHeader(name = "X-Tenant-Id", defaultValue = "demo") String tenantId,
            @RequestHeader(name = "X-User-Id", defaultValue = "user-1") String userId) {
        return accountService.currentIdentity().orElseGet(() -> accountService.load(tenantId, userId));
    }
}
