package com.aequitas.aequitascentralservice.app.port.inbound;

import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.List;
import java.util.Optional;

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
}
