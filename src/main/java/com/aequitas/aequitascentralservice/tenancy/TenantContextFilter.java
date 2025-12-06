package com.aequitas.aequitascentralservice.tenancy;

import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Captures the authenticated principal and stores it in a thread-local for downstream infrastructure.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantContextFilter extends OncePerRequestFilter {

    private final CurrentUserPort currentUserPort;

    public TenantContextFilter(final CurrentUserPort currentUserPort) {
        this.currentUserPort = currentUserPort;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/v3/api-docs")
                || request.getRequestURI().startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }
        final CurrentUser currentUser;
        try {
            currentUser = currentUserPort.currentUser();
        } catch (IllegalStateException ex) {
            filterChain.doFilter(request, response);
            return;
        }
        TenantContextHolder.setCurrentUser(currentUser);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
