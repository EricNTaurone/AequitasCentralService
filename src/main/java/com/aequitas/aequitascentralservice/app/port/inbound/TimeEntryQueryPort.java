package com.aequitas.aequitascentralservice.app.port.inbound;

import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.model.TimeEntryFilter;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port for read-side time entry operations.
 */
public interface TimeEntryQueryPort {

    /**
     * Fetches a single entry scoped to the caller's tenant.
     *
     * @param id entry identifier.
     * @return optional entry.
     */
    Optional<TimeEntry> findById(UUID id);

    /**
     * Queries paginated entries with firm and role awareness applied.
     *
     * @param filter filter criteria.
     * @param pageRequest pagination primitives.
     * @return page of entries the caller may observe.
     */
    PageResult<TimeEntry> search(TimeEntryFilter filter, PageRequest pageRequest);
}
