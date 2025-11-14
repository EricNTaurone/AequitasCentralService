package com.aequitas.aequitascentralservice.domain.value;

import java.util.UUID;

/**
 * Domain representation of the authenticated principal extracted from the JWT.
 *
 * @param userId unique identifier of the user within the firm.
 * @param firmId tenant identifier enforcing data isolation.
 * @param role   RBAC role used for authorization checks.
 */
public record CurrentUser(UUID userId, UUID firmId, Role role) {
}
