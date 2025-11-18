package com.aequitas.aequitascentralservice.domain.model;

import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.UUID;
import lombok.Builder;

/**
 * Representation of a Supabase user enriched with tenant metadata.
 *
 * @param id Supabase user identifier.
 * @param firmId tenant identifier stored in Supabase metadata.
 * @param email login email address.
 * @param role domain role as persisted in Supabase metadata.
 */
@Builder
public record SupabaseUser(UUID id, UUID firmId, String email, Role role) {}
