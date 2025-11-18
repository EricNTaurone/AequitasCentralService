package com.aequitas.aequitascentralservice.domain.model;

import com.aequitas.aequitascentralservice.domain.value.AuthTokens;

/**
 * Captures the Supabase-authenticated user and associated tokens returned from the provider.
 *
 * @param user Supabase user enriched with firm metadata.
 * @param tokens bearer tokens issued for the session.
 */
public record SupabaseAuthSession(SupabaseUser user, AuthTokens tokens) {}
