package com.aequitas.aequitascentralservice.app.service;

import com.aequitas.aequitascentralservice.app.port.inbound.TimeEntryCommandPort;
import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CustomerRepositoryPort;
import com.aequitas.aequitascentralservice.app.port.outbound.OutboxPort;
import com.aequitas.aequitascentralservice.app.port.outbound.ProjectRepositoryPort;
import com.aequitas.aequitascentralservice.app.port.outbound.TimeEntryRepositoryPort;
import com.aequitas.aequitascentralservice.domain.command.CreateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.event.EntryApprovedEvent;
import com.aequitas.aequitascentralservice.domain.model.Customer;
import com.aequitas.aequitascentralservice.domain.model.Project;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the command-side flows for the time entry aggregate while enforcing RBAC and tenancy.
 */
@Service
@Transactional
public class TimeEntryCommandService implements TimeEntryCommandPort {

    private final TimeEntryRepositoryPort repositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final ProjectRepositoryPort projectRepositoryPort;
    private final OutboxPort outboxPort;
    private final CurrentUserPort currentUserPort;
    private final ClockPort clockPort;

    public TimeEntryCommandService(
            final TimeEntryRepositoryPort repositoryPort,
            final CustomerRepositoryPort customerRepositoryPort,
            final ProjectRepositoryPort projectRepositoryPort,
            final OutboxPort outboxPort,
            final CurrentUserPort currentUserPort,
            final ClockPort clockPort) {
        this.repositoryPort = repositoryPort;
        this.customerRepositoryPort = customerRepositoryPort;
        this.projectRepositoryPort = projectRepositoryPort;
        this.outboxPort = outboxPort;
        this.currentUserPort = currentUserPort;
        this.clockPort = clockPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID create(final CreateTimeEntryCommand command) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        final Customer customer = requireCustomer(command.customerId(), currentUser);
        final Project project = requireProject(command.projectId(), currentUser);
        ensureProjectBelongsToCustomer(project, customer);
        final Instant now = clockPort.now();
        final TimeEntry entry =
                TimeEntry.draft(
                        currentUser.firmId(),
                        currentUser.userId(),
                        customer.id(),
                        project.id(),
                        command.matterId(),
                        command.narrative(),
                        command.durationMinutes(),
                        now);
        return repositoryPort.save(entry).id();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final UUID id, final UpdateTimeEntryCommand command) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        TimeEntry entry = repositoryPort
                .findById(id, currentUser.firmId())
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found"));
        ensureCanModify(currentUser, entry);

        final UUID targetCustomerId =
                command.customerId().orElse(entry.customerId());
        final UUID targetProjectId =
                command.projectId().orElse(entry.projectId());
        final UUID targetMatterId =
                command.matterId().orElse(entry.matterId());
        final String targetNarrative =
                command.narrative().orElse(entry.narrative());
        final int targetDuration =
                command.durationMinutes().orElse(entry.durationMinutes());

        final Customer customer = requireCustomer(targetCustomerId, currentUser);
        final Project project = requireProject(targetProjectId, currentUser);
        ensureProjectBelongsToCustomer(project, customer);

        entry =
                entry.updateDetails(
                        targetNarrative,
                        customer.id(),
                        project.id(),
                        targetMatterId,
                        targetDuration,
                        clockPort.now());
        repositoryPort.save(entry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit(final UUID id) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        TimeEntry entry = repositoryPort
                .findById(id, currentUser.firmId())
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found"));
        ensureEmployeeOwnsEntry(currentUser, entry);
        entry = entry.submit(clockPort.now());
        repositoryPort.save(entry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void approve(final UUID id) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        if (currentUser.role() == Role.EMPLOYEE) {
            throw new IllegalStateException("Employees cannot approve entries");
        }
        TimeEntry entry = repositoryPort
                .findById(id, currentUser.firmId())
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found"));
        ensureManagerWindow(currentUser, entry);
        entry = entry.approve(currentUser.userId(), clockPort.now());
        repositoryPort.save(entry);
        outboxPort.append(
                currentUser.firmId(),
                entry.id(),
                EntryApprovedEvent.from(entry));
    }

    private Customer requireCustomer(final UUID id, final CurrentUser currentUser) {
        return customerRepositoryPort
                .findById(id, currentUser.firmId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    private Project requireProject(final UUID id, final CurrentUser currentUser) {
        return projectRepositoryPort
                .findById(id, currentUser.firmId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    private void ensureProjectBelongsToCustomer(final Project project, final Customer customer) {
        if (!project.customerId().equals(customer.id())) {
            throw new IllegalArgumentException("Project does not belong to customer");
        }
    }

    private void ensureCanModify(final CurrentUser user, final TimeEntry entry) {
        if (user.role() == Role.ADMIN) {
            return;
        }
        if (user.role() == Role.MANAGER && entry.status() != EntryStatus.APPROVED) {
            return;
        }
        ensureEmployeeOwnsEntry(user, entry);
    }

    private void ensureEmployeeOwnsEntry(final CurrentUser user, final TimeEntry entry) {
        if (!entry.userId().equals(user.userId())) {
            throw new IllegalStateException("Employees can only modify their own entries");
        }
    }

    private void ensureManagerWindow(final CurrentUser user, final TimeEntry entry) {
        if (user.role() == Role.ADMIN) {
            return;
        }
        if (entry.status() == EntryStatus.APPROVED) {
            throw new IllegalStateException("Approved entries are immutable");
        }
    }
}
