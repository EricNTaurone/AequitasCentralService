package com.aequitas.aequitascentralservice.domain.event;

import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Canonical payload emitted when an entry transitions to APPROVED.
 *
 * @param eventId unique identifier for the event row.
 * @param occurredAt timestamp when the approval occurred.
 * @param firmId tenant identifier to group FIFO delivery.
 * @param payload event payload flattened into a JSON-friendly map.
 */
public record EntryApprovedEvent(
        UUID eventId, Instant occurredAt, UUID firmId, Map<String, Object> payload)
        implements DomainEvent {

    /**
     * Creates the event from a domain aggregate snapshot.
     *
     * @param entry approved entry snapshot.
     * @return immutable event.
     */
    public static EntryApprovedEvent from(final TimeEntry entry) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("entryId", entry.id().toString());
        payload.put("firmId", entry.firmId().toString());
        payload.put("userId", entry.userId().toString());
        payload.put("customerId", entry.customerId().toString());
        payload.put("projectId", entry.projectId().toString());
        payload.put("matterId", entry.matterId() == null ? null : entry.matterId().toString());
        payload.put("narrative", entry.narrative());
        payload.put("durationMinutes", entry.durationMinutes());
        payload.put("status", EntryStatus.APPROVED.name());
        payload.put("approvedAt", entry.approvedAt().toString());
        return new EntryApprovedEvent(
                UUID.randomUUID(),
                entry.approvedAt(),
                entry.firmId(),
                Collections.unmodifiableMap(payload));
    }

    @Override
    public String eventType() {
        return "ENTRY_APPROVED.v1";
    }
}
