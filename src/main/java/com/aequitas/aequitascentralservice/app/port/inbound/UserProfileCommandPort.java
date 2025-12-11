package com.aequitas.aequitascentralservice.app.port.inbound;

import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.Role;

import java.util.UUID;

/**
 * Inbound port describing user management commands.
 */
public interface UserProfileCommandPort {

    /**
     * Updates the target user's role within the tenant.
     *
     * @param userId  identifier of the user to update.
     * @param newRole new role assignment.
     */
    void updateRole(UUID userId, Role newRole);

    /**
     * Creates a new user profile.
     *
     * @param userProfile The user profile to create.
     * @return The newly created user profile.
     */
    UserProfile createUserProfile(UserProfile userProfile);

}
