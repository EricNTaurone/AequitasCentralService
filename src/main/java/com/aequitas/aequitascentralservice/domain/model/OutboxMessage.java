package com.aequitas.aequitascentralservice.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain representation of an outbox row awaiting relay.
 *
 * @param id unique identifier.
 * @param firmId tenant group identifier.
 * @param aggregateId identifier of the aggregate that produced the event.
 * @param eventType canonical event name.
 * @param payloadJson serialized JSON payload.
 * @param occurredAt time when domain change happened.
 * @param publishedAt timestamp when the relay successfully published the event.
 */
public record OutboxMessage(
        UUID id,
        UUID firmId,
        UUID aggregateId,
        String eventType,
        String payloadJson,
        Instant occurredAt,
        Instant publishedAt) {
}
