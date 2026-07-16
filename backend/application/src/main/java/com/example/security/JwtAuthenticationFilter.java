package com.example.security;

import com.example.account.AccountService;
import com.example.rbac.entity.SysUser;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AccountService accountService;

    public JwtAuthenticationFilter(JwtService jwtService, AccountService accountService) {
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                JwtService.Claims claims = jwtService.verify(authorization.substring(7));
                SysUser user = accountService.findById(claims.getUserId())
                        .filter(value -> Boolean.TRUE.equals(value.getEnabled()))
                        .filter(value -> value.getTenantId().equals(claims.getTenantId()))
                        .filter(value -> value.getUsername().equals(claims.getUsername()))
                        .orElseThrow(() -> new IllegalArgumentException("account disabled or missing"));
                AuthenticatedAccount principal = accountService.authenticated(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getPermissions().stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
