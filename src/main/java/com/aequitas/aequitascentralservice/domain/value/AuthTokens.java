package com.aequitas.aequitascentralservice.domain.value;

/**
 * Represents the bearer tokens issued by Supabase for authenticated sessions.
 *
 * @param accessToken short-lived JWT used for resource access.
 * @param refreshToken long-lived token to obtain new access tokens.
 * @param expiresInSeconds validity window for the access token, in seconds.
 * @param tokenType token prefix (e.g. {@code bearer}).
 */
public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {}
