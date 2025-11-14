package com.aequitas.aequitascentralservice.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO describing the payload required to create a draft entry.
 *
 * @param customerId customer identifier.
 * @param projectId project identifier.
 * @param matterId linked matter identifier.
 * @param narrative textual narrative.
 * @param durationMinutes duration in minutes.
 */
public record CreateTimeEntryRequest(
        @NotNull UUID customerId,
        @NotNull UUID projectId,
        UUID matterId,
        @NotBlank @Size(min = 1, max = 2048) String narrative,
        @NotNull Integer durationMinutes) {
}
