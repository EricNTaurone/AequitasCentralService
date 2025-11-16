package com.aequitas.aequitascentralservice.adapter.web;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.Role;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UserProfileResponse;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;

import lombok.experimental.UtilityClass;

/**
 * Maps user profile aggregates to REST DTOs.
 */
@UtilityClass
public class UserProfileDtoMapper {

    public static UserProfileResponse toResponse(final UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.id())
                .firmId(profile.firmId())
                .email(profile.email())
                .role(Role.valueOf(profile.role().name()))
                .build();

    }
}
