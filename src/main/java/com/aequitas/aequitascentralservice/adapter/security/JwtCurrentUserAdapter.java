package com.aequitas.aequitascentralservice.adapter.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;

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
        final UUID firmId = extractUuidClaim(jwt, "firm_id");
        final Role role = extractRole(jwt);
        return new CurrentUser(userId, firmId, role);
    }

    private UUID extractUuidClaim(final Jwt jwt, final String claimName) {
        final Object direct = jwt.getClaim(claimName);
        if (direct != null && StringUtils.hasText(String.valueOf(direct))) {
            return UUID.fromString(String.valueOf(direct));
        }
        final Object userMetadata = jwt.getClaim("user_metadata");
        if (userMetadata instanceof java.util.Map<?, ?> metadata) {
            final Object nested = metadata.get(claimName);
            if (nested != null && StringUtils.hasText(String.valueOf(nested))) {
                return UUID.fromString(String.valueOf(nested));
            }
        }
        final Object appMetadata = jwt.getClaim("app_metadata");
        if (appMetadata instanceof java.util.Map<?, ?> metadata) {
            final Object nested = metadata.get(claimName);
            if (nested != null && StringUtils.hasText(String.valueOf(nested))) {
                return UUID.fromString(String.valueOf(nested));
            }
        }
        throw new IllegalStateException("Missing required claim: " + claimName);
    }

    private Role extractRole(final Jwt jwt) {
        final String direct = jwt.getClaimAsString("role");
        if (StringUtils.hasText(direct)) {
            return Role.valueOf(direct.trim().toUpperCase());
        }
        final Object userMetadata = jwt.getClaim("user_metadata");
        if (userMetadata instanceof java.util.Map<?, ?> metadata) {
            final Object nested = metadata.get("role");
            if (nested != null && StringUtils.hasText(String.valueOf(nested))) {
                return Role.valueOf(String.valueOf(nested).trim().toUpperCase());
            }
        }
        final Object appMetadata = jwt.getClaim("app_metadata");
        if (appMetadata instanceof java.util.Map<?, ?> metadata) {
            final Object nested = metadata.get("role");
            if (nested != null && StringUtils.hasText(String.valueOf(nested))) {
                return Role.valueOf(String.valueOf(nested).trim().toUpperCase());
            }
        }
        throw new IllegalStateException("Missing required role claim");
    }
}
