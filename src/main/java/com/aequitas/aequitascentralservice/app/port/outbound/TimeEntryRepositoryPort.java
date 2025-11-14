package com.aequitas.aequitascentralservice.app.port.outbound;

import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.model.TimeEntryFilter;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstraction over persistence for the time entry aggregate.
 */
public interface TimeEntryRepositoryPort {

    /**
     * Persists the aggregate snapshot.
     *
     * @param entry aggregate snapshot.
     * @return saved snapshot.
     */
    TimeEntry save(TimeEntry entry);

    /**
     * Loads an entry limited to a single firm.
     *
     * @param id entry identifier.
     * @param firmId tenant identifier.
     * @return optional entry.
     */
    Optional<TimeEntry> findById(UUID id, UUID firmId);

    /**
     * Searches entries scoped to a tenant using the provided filter.
     *
     * @param firmId tenant identifier.
     * @param filter filter criteria.
     * @param pageRequest pagination primitives.
     * @return immutable page result.
     */
    PageResult<TimeEntry> search(UUID firmId, TimeEntryFilter filter, PageRequest pageRequest);
}
