package com.aequitas.aequitascentralservice.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CustomerRepositoryPort;
import com.aequitas.aequitascentralservice.app.port.outbound.OutboxPort;
import com.aequitas.aequitascentralservice.app.port.outbound.ProjectRepositoryPort;
import com.aequitas.aequitascentralservice.app.port.outbound.TimeEntryRepositoryPort;
import com.aequitas.aequitascentralservice.domain.command.CreateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.event.DomainEvent;
import com.aequitas.aequitascentralservice.domain.model.Customer;
import com.aequitas.aequitascentralservice.domain.model.Project;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link TimeEntryCommandService}.
 */
@ExtendWith(MockitoExtension.class)
class TimeEntryCommandServiceTest {

    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");

    @Mock private TimeEntryRepositoryPort timeEntryRepositoryPort;
    @Mock private CustomerRepositoryPort customerRepositoryPort;
    @Mock private ProjectRepositoryPort projectRepositoryPort;
    @Mock private OutboxPort outboxPort;
    @Mock private CurrentUserPort currentUserPort;
    @Mock private ClockPort clockPort;

    @Captor private ArgumentCaptor<TimeEntry> timeEntryCaptor;
    @Captor private ArgumentCaptor<DomainEvent> eventCaptor;

    private TimeEntryCommandService service;
    private CurrentUser employee;
    private Customer customer;
    private Project project;

    @BeforeEach
    void setUp() {
        service =
                new TimeEntryCommandService(
                        timeEntryRepositoryPort,
                        customerRepositoryPort,
                        projectRepositoryPort,
                        outboxPort,
                        currentUserPort,
                        clockPort);
        employee = new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), Role.EMPLOYEE);
        customer = new Customer(UUID.randomUUID(), employee.firmId(), "Acme", NOW);
        project =
                new Project(
                        UUID.randomUUID(),
                        employee.firmId(),
                        customer.id(),
                        "Litigation",
                        "ACTIVE",
                        NOW);
        when(currentUserPort.currentUser()).thenReturn(employee);
    }

    @Test
    @DisplayName("create should persist draft entries for the current user")
    void GIVEN_validCreateCommand_WHEN_create_THEN_draftSaved() {
        // GIVEN
        when(clockPort.now()).thenReturn(NOW);
        when(customerRepositoryPort.findById(customer.id(), employee.firmId()))
                .thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(project.id(), employee.firmId()))
                .thenReturn(Optional.of(project));
        when(timeEntryRepositoryPort.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        service.create(new CreateTimeEntryCommand(customer.id(), project.id(), null, "Draft terms", 30));

        // THEN
        verify(currentUserPort, times(1)).currentUser();
        verify(customerRepositoryPort, times(1)).findById(customer.id(), employee.firmId());
        verify(projectRepositoryPort, times(1)).findById(project.id(), employee.firmId());
        verify(clockPort, times(1)).now();
        verify(timeEntryRepositoryPort, times(1)).save(timeEntryCaptor.capture());
        TimeEntry saved = timeEntryCaptor.getValue();
        assertEquals(employee.firmId(), saved.firmId());
        assertEquals(employee.userId(), saved.userId());
        verifyNoMoreInteractions(
                timeEntryRepositoryPort,
                customerRepositoryPort,
                projectRepositoryPort,
                outboxPort,
                currentUserPort,
                clockPort);
    }

    @Test
    @DisplayName("approve should reject employees")
    void GIVEN_employee_WHEN_approve_THEN_exceptionThrown() {
        // GIVEN
        UUID entryId = UUID.randomUUID();

        // WHEN
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> service.approve(entryId));

        // THEN
        assertEquals("Employees cannot approve entries", exception.getMessage());
        verify(currentUserPort, times(1)).currentUser();
        verifyNoMoreInteractions(
                timeEntryRepositoryPort,
                customerRepositoryPort,
                projectRepositoryPort,
                outboxPort,
                currentUserPort,
                clockPort);
    }

    @Test
    @DisplayName("manager can approve after submission")
    void GIVEN_manager_WHEN_approve_THEN_entryApprovedAndEventPublished() {
        // GIVEN
        CurrentUser manager = new CurrentUser(UUID.randomUUID(), employee.firmId(), Role.MANAGER);
        when(currentUserPort.currentUser()).thenReturn(manager);
        when(clockPort.now()).thenReturn(NOW);
        UUID entryId = UUID.randomUUID();
        TimeEntry entry =
                TimeEntry.draft(
                                manager.firmId(),
                                employee.userId(),
                                customer.id(),
                                project.id(),
                                null,
                                "Draft",
                                60,
                                NOW)
                        .submit(NOW);
        when(timeEntryRepositoryPort.findById(entryId, manager.firmId()))
                .thenReturn(Optional.of(entry));

        // WHEN
        service.approve(entryId);

        // THEN
        verify(currentUserPort, times(1)).currentUser();
        verify(timeEntryRepositoryPort, times(1)).findById(entryId, manager.firmId());
        verify(clockPort, times(1)).now();
        verify(timeEntryRepositoryPort, times(1)).save(timeEntryCaptor.capture());
        verify(outboxPort, times(1)).append(eq(manager.firmId()), eq(entry.id()), eventCaptor.capture());
        TimeEntry approved = timeEntryCaptor.getValue();
        assertEquals(EntryStatus.APPROVED, approved.status());
        assertEquals("ENTRY_APPROVED.v1", eventCaptor.getValue().eventType());
        verifyNoMoreInteractions(
                timeEntryRepositoryPort,
                customerRepositoryPort,
                projectRepositoryPort,
                outboxPort,
                currentUserPort,
                clockPort);
    }

    @Test
    @DisplayName("update should forbid employees touching other users' entries")
    void GIVEN_employeeEditingOthersEntry_WHEN_update_THEN_exceptionThrown() {
        // GIVEN
        UUID entryId = UUID.randomUUID();
        TimeEntry otherEntry =
                TimeEntry.rehydrate(
                        entryId,
                        employee.firmId(),
                        UUID.randomUUID(),
                        customer.id(),
                        project.id(),
                        null,
                        "Narrative",
                        30,
                        EntryStatus.DRAFT,
                        NOW,
                        NOW,
                        null,
                        null);
        when(timeEntryRepositoryPort.findById(entryId, employee.firmId()))
                .thenReturn(Optional.of(otherEntry));

        // WHEN
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.update(entryId, new UpdateTimeEntryCommand(
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of("new"),
                                Optional.empty())));

        // THEN
        assertEquals("Employees can only modify their own entries", exception.getMessage());
        verify(currentUserPort, times(1)).currentUser();
        verify(timeEntryRepositoryPort, times(1)).findById(entryId, employee.firmId());
        verifyNoMoreInteractions(
                timeEntryRepositoryPort,
                customerRepositoryPort,
                projectRepositoryPort,
                outboxPort,
                currentUserPort,
                clockPort);
    }
}
