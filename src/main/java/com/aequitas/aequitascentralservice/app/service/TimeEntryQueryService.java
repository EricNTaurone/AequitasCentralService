package com.aequitas.aequitascentralservice.app.service;

import com.aequitas.aequitascentralservice.app.port.inbound.TimeEntryQueryPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.TimeEntryRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.model.TimeEntryFilter;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the read side queries for time entries with firm-aware scoping.
 */
@Service
@Transactional(readOnly = true)
public class TimeEntryQueryService implements TimeEntryQueryPort {

    private final TimeEntryRepositoryPort repositoryPort;
    private final CurrentUserPort currentUserPort;

    public TimeEntryQueryService(
            final TimeEntryRepositoryPort repositoryPort, final CurrentUserPort currentUserPort) {
        this.repositoryPort = repositoryPort;
        this.currentUserPort = currentUserPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TimeEntry> findById(final UUID id) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        return repositoryPort.findById(id, currentUser.firmId())
                .filter(entry -> canObserve(currentUser, entry));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<TimeEntry> search(final TimeEntryFilter filter, final PageRequest pageRequest) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        final TimeEntryFilter enrichedFilter = enrichFilter(filter, currentUser);
        return repositoryPort.search(currentUser.firmId(), enrichedFilter, pageRequest);
    }

    private boolean canObserve(final CurrentUser user, final TimeEntry entry) {
        return user.role() != Role.EMPLOYEE || entry.userId().equals(user.userId());
    }

    private TimeEntryFilter enrichFilter(final TimeEntryFilter filter, final CurrentUser user) {
        if (user.role() == Role.EMPLOYEE) {
            return new TimeEntryFilter(
                    filter.customerId(), filter.projectId(), filter.status(), Optional.of(user.userId()));
        }
        return filter;
    }
}
