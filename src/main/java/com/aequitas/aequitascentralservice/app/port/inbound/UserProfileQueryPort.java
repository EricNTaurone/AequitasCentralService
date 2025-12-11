package com.aequitas.aequitascentralservice.app.port.inbound;

import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port for querying firm-scoped user profiles.
 */
public interface UserProfileQueryPort {

    /**
     * @return the authenticated user's profile.
     */
    UserProfile me();

    /**
     * Lists firm users available to the caller given their permissions.
     *
     * @param role optional role filter.
     * @return immutable list.
     */
    List<UserProfile> list(Optional<Role> role);

    /**
     * Fetches a user profile by authentication ID.
     * @param authenticationId A UUID authentication Id of the user.
     *                         This is the Id of the user in the authentication system.
     * @return a User Profile
     */
    UserProfile findByAuthenticationId(UUID authenticationId);
}
