package com.aequitas.aequitascentralservice.domain.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

/**
 * Aggregate representing scoped projects that roll up to customers.
 *
 * @param id unique identifier.
 * @param firmId tenant identifier.
 * @param customerId owning customer identifier.
 * @param name project display name.
 * @param status textual status (e.g., ACTIVE).
 * @param createdAt creation timestamp.
 */
@Builder
public record Project(
        UUID id, UUID firmId, UUID customerId, String name, String status, Instant createdAt) {
}
