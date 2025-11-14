package com.aequitas.aequitascentralservice.adapter.web.dto;

import com.aequitas.aequitascentralservice.domain.value.Role;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for updating a user's role assignment.
 *
 * @param role new role to apply.
 */
public record UpdateUserRoleRequest(@NotNull Role role) {
}
