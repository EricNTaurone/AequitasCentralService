package com.aequitas.aequitascentralservice.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker interface for immutable domain events persisted in the outbox.
 */
public interface DomainEvent {
    /**
     * @return globally unique identifier for the event.
     */
    UUID eventId();

    /**
     * @return ISO 8601 timestamp representing when the event occurred.
     */
    Instant occurredAt();

    /**
     * @return canonical event type name (e.g., ENTRY_APPROVED.v1).
     */
    String eventType();
}
