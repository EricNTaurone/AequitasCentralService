package com.aequitas.aequitascentralservice.domain.model;

import com.aequitas.aequitascentralservice.domain.value.AuthTokens;

import lombok.Builder;

/**
 * Aggregates the authenticated principal's profile with the issued tokens.
 *
 * @param profile persisted tenant-scoped profile.
 * @param tokens Supabase-issued bearer tokens for subsequent requests.
 */
@Builder
public record AuthSession(UserProfile profile, AuthTokens tokens) {}
