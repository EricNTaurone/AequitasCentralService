package com.aequitas.aequitascentralservice.app.port.outbound;

import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence abstraction for user profile aggregates.
 */
public interface UserProfileRepositoryPort {

    /**
     * Finds a profile by id within the same firm.
     *
     * @param id     user identifier.
     * @param firmId tenant identifier.
     * @return optional profile.
     */
    Optional<UserProfile> findById(UUID id, UUID firmId);


    /**
     * Finds a User Profile by authentication ID.
     *
     * @param authenticationId The authentication ID of the user.
     *                         this is their Id in an authentication system
     * @return Optional User Profile
     */
    Optional<UserProfile> findByAuthenticationId(UUID authenticationId);


    /**
     * Lists all profiles belonging to a firm.
     *
     * @param firmId tenant identifier.
     * @return immutable list.
     */
    List<UserProfile> findByFirmId(UUID firmId);

    /**
     * Lists profiles for a firm filtered by role.
     *
     * @param firmId tenant identifier.
     * @param role   role filter.
     * @return immutable list.
     */
    List<UserProfile> findByFirmIdAndRole(UUID firmId, Role role);

    /**
     * Persists the supplied profile snapshot.
     *
     * @param profile domain profile.
     * @return saved snapshot.
     */
    UserProfile save(UserProfile profile);
}
