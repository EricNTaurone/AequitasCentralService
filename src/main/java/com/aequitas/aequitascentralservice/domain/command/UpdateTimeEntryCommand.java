package com.aequitas.aequitascentralservice.domain.command;

import java.util.Optional;
import java.util.UUID;

import lombok.Builder;

/**
 * Command describing optional entry attributes that may be patched by the caller.
 *
 * @param customerId optional new customer reference.
 * @param projectId  optional new project reference.
 * @param matterId   optional new matter reference identifier.
 * @param narrative  optional narrative override.
 * @param durationMinutes optional new duration in minutes.
 */
@Builder
public record UpdateTimeEntryCommand(
        Optional<UUID> customerId,
        Optional<UUID> projectId,
        Optional<UUID> matterId,
        Optional<String> narrative,
        Optional<Integer> durationMinutes) {
}
