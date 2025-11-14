package com.aequitas.aequitascentralservice.app.port.inbound;

import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.UUID;

/**
 * Inbound port describing user management commands.
 */
public interface UserProfileCommandPort {

    /**
     * Updates the target user's role within the tenant.
     *
     * @param userId identifier of the user to update.
     * @param newRole new role assignment.
     */
    void updateRole(UUID userId, Role newRole);
}
