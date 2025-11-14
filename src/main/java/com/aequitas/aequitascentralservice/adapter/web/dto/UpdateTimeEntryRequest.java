package com.aequitas.aequitascentralservice.adapter.web.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO for partial updates to an entry.
 *
 * @param customerId optional customer identifier.
 * @param projectId optional project identifier.
 * @param matterId optional matter identifier.
 * @param narrative optional narrative override.
 * @param durationMinutes optional duration in minutes.
 */
public record UpdateTimeEntryRequest(
        UUID customerId,
        UUID projectId,
        UUID matterId,
        @Size(min = 1, max = 2048) String narrative,
        Integer durationMinutes) {
}
