package com.aequitas.aequitascentralservice.domain.model;

import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import java.util.Optional;
import java.util.UUID;

/**
 * Filter criteria used when querying paginated time entry lists.
 *
 * @param customerId optional customer identifier filter.
 * @param projectId  optional project identifier filter.
 * @param status     optional status filter.
 * @param ownerId    optional owner filter used by managers/admins.
 */
public record TimeEntryFilter(
        Optional<UUID> customerId,
        Optional<UUID> projectId,
        Optional<EntryStatus> status,
        Optional<UUID> ownerId) {
}
