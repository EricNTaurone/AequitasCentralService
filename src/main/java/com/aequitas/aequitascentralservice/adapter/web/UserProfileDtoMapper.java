package com.aequitas.aequitascentralservice.adapter.web;

import com.aequitas.aequitascentralservice.adapter.web.dto.UserProfileResponse;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Maps user profile aggregates to REST DTOs.
 */
@Component
public class UserProfileDtoMapper {

    public UserProfileResponse toResponse(final UserProfile profile) {
        return new UserProfileResponse(profile.id(), profile.firmId(), profile.email(), profile.role());
    }
}
