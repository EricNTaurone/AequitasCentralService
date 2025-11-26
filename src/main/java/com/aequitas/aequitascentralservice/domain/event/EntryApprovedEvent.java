package com.aequitas.aequitascentralservice.domain.event;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.aequitas.aequitascentralservice.constants.EntryFieldConstants;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;

import lombok.Builder;

/**
 * Canonical payload emitted when an entry transitions to APPROVED.
 *
 * @param eventId unique identifier for the event row.
 * @param occurredAt timestamp when the approval occurred.
 * @param firmId tenant identifier to group FIFO delivery.
 * @param payload event payload flattened into a JSON-friendly map.
 */
@Builder
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
        payload.put(EntryFieldConstants.ENTRY_ID, entry.getId().toString());
        payload.put(EntryFieldConstants.FIRM_ID, entry.getFirmId().toString());
        payload.put(EntryFieldConstants.USER_ID, entry.getUserId().toString());
        payload.put(EntryFieldConstants.CUSTOMER_ID, entry.getCustomerId().toString());
        payload.put(EntryFieldConstants.PROJECT_ID, entry.getProjectId().toString());
        payload.put(EntryFieldConstants.MATTER_ID, entry.getMatterId() == null ? null : entry.getMatterId().toString());
        payload.put(EntryFieldConstants.NARRATIVE, entry.getNarrative());
        payload.put(EntryFieldConstants.DURATION_MINUTES, entry.getDurationMinutes());
        payload.put(EntryFieldConstants.STATUS, EntryStatus.APPROVED.name());
        payload.put(EntryFieldConstants.APPROVED_AT, entry.getApprovedAt().toString());
        return new EntryApprovedEvent(
                UUID.randomUUID(),
                entry.getApprovedAt(),
                entry.getFirmId(),
                Collections.unmodifiableMap(payload));
    }

    @Override
    public String eventType() {
        return "ENTRY_APPROVED.v1";
    }
}
