package com.aequitas.aequitascentralservice.adapter.web;


import com.aequitas.aequitascentralservice.adapter.web.generated.dto.AuthResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.TokenResponse;
import com.aequitas.aequitascentralservice.domain.model.AuthSession;

import lombok.experimental.UtilityClass;

/**
 * Maps authentication domain sessions to REST DTOs.
 */
@UtilityClass
public final class AuthenticationDtoMapper {

    public static AuthResponse toResponse(final AuthSession session) {
        return new AuthResponse(
                UserProfileDtoMapper.toResponse(session.profile()),
                new TokenResponse(
                        session.tokens().accessToken(),
                        session.tokens().refreshToken(),
                        session.tokens().expiresInSeconds(),
                        session.tokens().tokenType()));
    }
}
