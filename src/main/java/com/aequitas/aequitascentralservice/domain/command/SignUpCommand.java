package com.aequitas.aequitascentralservice.domain.command;

import java.util.UUID;

import com.aequitas.aequitascentralservice.domain.value.Role;

import lombok.Builder;

/**
 * Command carrying the inputs required to create a new authenticated user through Supabase and
 * bootstrap their tenant-scoped profile.
 *
 * @param firmId tenant identifier the user belongs to.
 * @param email login email to register with Supabase.
 * @param password credential to store in Supabase.
 * @param role domain role assigned inside the tenant.
 */
@Builder
public record SignUpCommand(UUID firmId, String email, String password, Role role) {}
