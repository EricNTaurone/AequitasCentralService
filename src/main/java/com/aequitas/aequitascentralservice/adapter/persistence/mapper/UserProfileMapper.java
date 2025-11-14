package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.UserProfileEntity;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;

/**
 * Maps {@link UserProfile} aggregates to/from JPA entities.
 */
public final class UserProfileMapper {

    private UserProfileMapper() {}

    public static UserProfile toDomain(final UserProfileEntity entity) {
        return new UserProfile(entity.getId(), entity.getFirmId(), entity.getEmail(), entity.getRole());
    }

    public static UserProfileEntity toEntity(final UserProfile profile) {
        final UserProfileEntity entity = new UserProfileEntity();
        entity.setId(profile.id());
        entity.setFirmId(profile.firmId());
        entity.setEmail(profile.email());
        entity.setRole(profile.role());
        return entity;
    }
}
