package com.aequitas.aequitascentralservice.domain.model;

import java.util.UUID;

import com.aequitas.aequitascentralservice.domain.value.Role;

import lombok.Builder;

/**
 * Represents a firm-scoped user profile maintained alongside Supabase auth identities.
 *
 * @param id unique user identifier.
 * @param firmId tenant identifier.
 * @param email login email address.
 * @param role assigned role for RBAC decisions.
 */
@Builder
public record UserProfile(UUID id, UUID firmId, String email, Role role) {
}
