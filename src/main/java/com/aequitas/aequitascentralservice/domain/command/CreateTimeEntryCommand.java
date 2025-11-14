package com.aequitas.aequitascentralservice.domain.command;

import java.util.UUID;

/**
 * Command describing the user-supplied attributes required to create a draft time entry.
 *
 * @param customerId identifier of the customer.
 * @param projectId  identifier of the project.
 * @param matterId   identifier of the matter in the downstream billing system.
 * @param narrative  textual narrative supplied by the user.
 * @param durationMinutes billable minutes captured by the entry.
 */
public record CreateTimeEntryCommand(
        UUID customerId, UUID projectId, UUID matterId, String narrative, int durationMinutes) {
}
