package com.aequitas.aequitascentralservice.adapter.web.dto;

import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.UUID;

/**
 * Response DTO for user profile operations.
 *
 * @param id user identifier.
 * @param firmId tenant identifier.
 * @param email email address.
 * @param role assigned role.
 */
public record UserProfileResponse(UUID id, UUID firmId, String email, Role role) {
}
