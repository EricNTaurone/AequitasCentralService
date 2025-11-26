package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.UserProfileEntity;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;

import lombok.experimental.UtilityClass;

/**
 * Maps {@link UserProfile} aggregates to/from JPA entities.
 */
@UtilityClass
public final class UserProfileMapper {

    public static UserProfile toDomain(final UserProfileEntity entity) {
        return UserProfile.builder()
                .id(entity.getId())
                .firmId(entity.getFirmId())
                .email(entity.getEmail())
                .role(entity.getRole())
                .build();
    }

    public static UserProfileEntity toEntity(final UserProfile profile) {
        return UserProfileEntity.builder()
                .id(profile.id())
                .firmId(profile.firmId())
                .email(profile.email())
                .role(profile.role())
                .build();
    }
}
