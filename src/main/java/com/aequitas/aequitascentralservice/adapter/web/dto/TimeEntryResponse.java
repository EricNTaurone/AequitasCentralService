package com.aequitas.aequitascentralservice.adapter.web.dto;

import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Read DTO returned by the API.
 *
 * @param id entry identifier.
 * @param customerId customer identifier.
 * @param projectId project identifier.
 * @param matterId matter identifier.
 * @param userId owner identifier.
 * @param narrative narrative.
 * @param durationMinutes duration.
 * @param status lifecycle status.
 * @param createdAt creation timestamp.
 * @param updatedAt last update timestamp.
 * @param approvedAt approval timestamp.
 */
public record TimeEntryResponse(
        UUID id,
        UUID customerId,
        UUID projectId,
        UUID matterId,
        UUID userId,
        String narrative,
        int durationMinutes,
        EntryStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant approvedAt) {
}
