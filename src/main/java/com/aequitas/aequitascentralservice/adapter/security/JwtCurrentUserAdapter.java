package com.aequitas.aequitascentralservice.adapter.security;

import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Maps the Spring Security {@link Jwt} principal into the domain {@link CurrentUser}.
 */
@Component
public class JwtCurrentUserAdapter implements CurrentUserPort {

    /**
     * {@inheritDoc}
     */
    @Override
    public CurrentUser currentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Missing JWT authentication");
        }
        final UUID userId = UUID.fromString(jwt.getSubject());
        final UUID firmId = UUID.fromString(jwt.getClaim("firm_id"));
        final Role role = Role.valueOf(jwt.getClaim("role"));
        return new CurrentUser(userId, firmId, role);
    }
}
