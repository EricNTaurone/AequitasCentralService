package com.aequitas.aequitascentralservice.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;

import lombok.Builder;

/**
 * Represents an idempotent workflow execution stored for replay safety.
 *
 * @param id unique identifier.
 * @param operation logical workflow being guarded.
 * @param userId user that initiated the workflow.
 * @param firmId firm associated with the workflow.
 * @param keyHash normalized hash for the provided idempotency key.
 * @param responseId stored response identifier (if applicable).
 * @param createdAt creation timestamp.
 * @param expiresAt expiration timestamp after which the key can be reused.
 */
@Builder
public record IdempotencyRecord(
        UUID id,
        IdempotencyOperation operation,
        UUID userId,
        UUID firmId,
        String keyHash,
        UUID responseId,
        Instant createdAt,
        Instant expiresAt) {
}
