package com.aequitas.aequitascentralservice.domain.model;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate representing a firm-owned customer that projects and entries reference.
 *
 * @param id unique identifier.
 * @param firmId tenant identifier.
 * @param name customer display name.
 * @param createdAt creation timestamp.
 */
@Builder
public record Customer(UUID id, UUID firmId, String name, Instant createdAt) {
}
