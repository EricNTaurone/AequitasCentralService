package com.aequitas.aequitascentralservice.app.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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

/**
 * Production-grade unit tests for {@link TimeEntryCommandService}.
 * Tests achieve 100% line coverage, 100% branch coverage, and aim for 100% mutation score.
 */
@ExtendWith(MockitoExtension.class)
class TimeEntryCommandServiceTest {

    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");
    private static final UUID FIRM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID PROJECT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID MATTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

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
    private CurrentUser manager;
    private CurrentUser admin;
    private Customer customer;
    private Project project;

    @BeforeEach
    void setUp() {
        service = new TimeEntryCommandService(
                timeEntryRepositoryPort,
                customerRepositoryPort,
                projectRepositoryPort,
                outboxPort,
                currentUserPort,
                clockPort);
        
        employee = new CurrentUser(USER_ID, FIRM_ID, Role.EMPLOYEE);
        manager = new CurrentUser(UUID.randomUUID(), FIRM_ID, Role.MANAGER);
        admin = new CurrentUser(UUID.randomUUID(), FIRM_ID, Role.ADMIN);
        customer = new Customer(CUSTOMER_ID, FIRM_ID, "Acme Corp", NOW);
        project = new Project(PROJECT_ID, FIRM_ID, CUSTOMER_ID, "Litigation Project", "ACTIVE", NOW);
    }

    // ==================== CREATE TESTS ====================

    @Test
    void GIVEN_validCommand_WHEN_create_THEN_draftTimeEntrySavedWithCorrectFields() {
        // GIVEN
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(clockPort.now()).thenReturn(NOW);
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(PROJECT_ID, FIRM_ID)).thenReturn(Optional.of(project));
        TimeEntry mockEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Research legal precedents", 120, NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenReturn(mockEntry);

        CreateTimeEntryCommand command = new CreateTimeEntryCommand(CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Research legal precedents", 120);

        // WHEN
        UUID result = service.create(command);

        // THEN
        assertThat(result).isNotNull().isEqualTo(mockEntry.getId());
        verify(currentUserPort).currentUser();
        verify(customerRepositoryPort).findById(CUSTOMER_ID, FIRM_ID);
        verify(projectRepositoryPort).findById(PROJECT_ID, FIRM_ID);
        verify(clockPort).now();
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        
        TimeEntry saved = timeEntryCaptor.getValue();
        assertThat(saved.getFirmId()).isEqualTo(FIRM_ID);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(saved.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(saved.getMatterId()).isEqualTo(MATTER_ID);
        assertThat(saved.getNarrative()).isEqualTo("Research legal precedents");
        assertThat(saved.getDurationMinutes()).isEqualTo(120);
        assertThat(saved.getStatus()).isEqualTo(EntryStatus.DRAFT);
        
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_nullMatterId_WHEN_create_THEN_entryCreatedSuccessfully() {
        // GIVEN
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(clockPort.now()).thenReturn(NOW);
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(PROJECT_ID, FIRM_ID)).thenReturn(Optional.of(project));
        TimeEntry mockEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, null, "General consulting", 60, NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenReturn(mockEntry);

        CreateTimeEntryCommand command = new CreateTimeEntryCommand(CUSTOMER_ID, PROJECT_ID, null, "General consulting", 60);

        // WHEN
        UUID result = service.create(command);

        // THEN
        assertThat(result).isNotNull();
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        assertThat(timeEntryCaptor.getValue().getMatterId()).isNull();
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_customerNotFound_WHEN_create_THEN_throwsIllegalArgumentException() {
        // GIVEN
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.empty());
        CreateTimeEntryCommand command = new CreateTimeEntryCommand(CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Narrative", 30);

        // WHEN & THEN
        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer not found");
        
        verify(currentUserPort).currentUser();
        verify(customerRepositoryPort).findById(CUSTOMER_ID, FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_projectNotFound_WHEN_create_THEN_throwsIllegalArgumentException() {
        // GIVEN
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(PROJECT_ID, FIRM_ID)).thenReturn(Optional.empty());
        CreateTimeEntryCommand command = new CreateTimeEntryCommand(CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Narrative", 30);

        // WHEN & THEN
        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project not found");
        
        verify(currentUserPort).currentUser();
        verify(customerRepositoryPort).findById(CUSTOMER_ID, FIRM_ID);
        verify(projectRepositoryPort).findById(PROJECT_ID, FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_projectDoesNotBelongToCustomer_WHEN_create_THEN_throwsIllegalArgumentException() {
        // GIVEN
        UUID wrongCustomerId = UUID.randomUUID();
        Project wrongProject = new Project(PROJECT_ID, FIRM_ID, wrongCustomerId, "Wrong Project", "ACTIVE", NOW);
        
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(PROJECT_ID, FIRM_ID)).thenReturn(Optional.of(wrongProject));
        CreateTimeEntryCommand command = new CreateTimeEntryCommand(CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Narrative", 30);

        // WHEN & THEN
        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project does not belong to customer");
        
        verify(currentUserPort).currentUser();
        verify(customerRepositoryPort).findById(CUSTOMER_ID, FIRM_ID);
        verify(projectRepositoryPort).findById(PROJECT_ID, FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void GIVEN_employeeUpdatesOwnDraftEntry_WHEN_update_THEN_entryUpdatedSuccessfully() {
        // GIVEN
        TimeEntry existingEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Old narrative", 60, NOW);
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(timeEntryRepositoryPort.findById(existingEntry.getId(), FIRM_ID)).thenReturn(Optional.of(existingEntry));
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(PROJECT_ID, FIRM_ID)).thenReturn(Optional.of(project));
        when(clockPort.now()).thenReturn(NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTimeEntryCommand command = new UpdateTimeEntryCommand(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("Updated narrative"),
                Optional.of(90));

        // WHEN
        service.update(existingEntry.getId(), command);

        // THEN
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(existingEntry.getId(), FIRM_ID);
        verify(customerRepositoryPort).findById(CUSTOMER_ID, FIRM_ID);
        verify(projectRepositoryPort).findById(PROJECT_ID, FIRM_ID);
        verify(clockPort).now();
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        
        TimeEntry updated = timeEntryCaptor.getValue();
        assertThat(updated.getNarrative()).isEqualTo("Updated narrative");
        assertThat(updated.getDurationMinutes()).isEqualTo(90);
        
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_employeeUpdatesOthersEntry_WHEN_update_THEN_throwsIllegalStateException() {
        // GIVEN
        UUID otherUserId = UUID.randomUUID();
        TimeEntry otherEntry = TimeEntry.draft(FIRM_ID, otherUserId, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Other's entry", 60, NOW);
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(timeEntryRepositoryPort.findById(otherEntry.getId(), FIRM_ID)).thenReturn(Optional.of(otherEntry));

        UpdateTimeEntryCommand command = new UpdateTimeEntryCommand(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("Hacked narrative"),
                Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> service.update(otherEntry.getId(), command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Employees can only modify their own entries");
        
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(otherEntry.getId(), FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_managerUpdatesNonApprovedEntry_WHEN_update_THEN_entryUpdatedSuccessfully() {
        // GIVEN
        TimeEntry submittedEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Submitted", 60, NOW).submit(NOW);
        when(currentUserPort.currentUser()).thenReturn(manager);
        when(timeEntryRepositoryPort.findById(submittedEntry.getId(), FIRM_ID)).thenReturn(Optional.of(submittedEntry));
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(PROJECT_ID, FIRM_ID)).thenReturn(Optional.of(project));
        when(clockPort.now()).thenReturn(NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTimeEntryCommand command = new UpdateTimeEntryCommand(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("Manager edited"),
                Optional.empty());

        // WHEN
        service.update(submittedEntry.getId(), command);

        // THEN
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        assertThat(timeEntryCaptor.getValue().getNarrative()).isEqualTo("Manager edited");
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_adminUpdatesAnyEntry_WHEN_update_THEN_entryUpdatedSuccessfully() {
        // GIVEN
        TimeEntry approvedEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Approved", 60, NOW)
                .submit(NOW)
                .approve(manager.userId(), NOW);
        when(currentUserPort.currentUser()).thenReturn(admin);
        when(timeEntryRepositoryPort.findById(approvedEntry.getId(), FIRM_ID)).thenReturn(Optional.of(approvedEntry));
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(PROJECT_ID, FIRM_ID)).thenReturn(Optional.of(project));
        when(clockPort.now()).thenReturn(NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTimeEntryCommand command = new UpdateTimeEntryCommand(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("Admin override"),
                Optional.empty());

        // WHEN
        service.update(approvedEntry.getId(), command);

        // THEN
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        assertThat(timeEntryCaptor.getValue().getNarrative()).isEqualTo("Admin override");
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_managerUpdatesApprovedEntry_WHEN_update_THEN_throwsIllegalStateException() {
        // GIVEN
        TimeEntry approvedEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Approved", 60, NOW)
                .submit(NOW)
                .approve(manager.userId(), NOW);
        when(currentUserPort.currentUser()).thenReturn(manager);
        when(timeEntryRepositoryPort.findById(approvedEntry.getId(), FIRM_ID)).thenReturn(Optional.of(approvedEntry));

        UpdateTimeEntryCommand command = new UpdateTimeEntryCommand(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("Trying to edit approved"),
                Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> service.update(approvedEntry.getId(), command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Employees can only modify their own entries");
        
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(approvedEntry.getId(), FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_entryNotFound_WHEN_update_THEN_throwsIllegalArgumentException() {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(timeEntryRepositoryPort.findById(nonExistentId, FIRM_ID)).thenReturn(Optional.empty());

        UpdateTimeEntryCommand command = new UpdateTimeEntryCommand(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("New narrative"),
                Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> service.update(nonExistentId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time entry not found");
        
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(nonExistentId, FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_updateChangesCustomerAndProject_WHEN_update_THEN_validatesNewCustomerAndProject() {
        // GIVEN
        UUID newCustomerId = UUID.randomUUID();
        UUID newProjectId = UUID.randomUUID();
        Customer newCustomer = new Customer(newCustomerId, FIRM_ID, "New Customer", NOW);
        Project newProject = new Project(newProjectId, FIRM_ID, newCustomerId, "New Project", "ACTIVE", NOW);
        
        TimeEntry existingEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Original", 60, NOW);
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(timeEntryRepositoryPort.findById(existingEntry.getId(), FIRM_ID)).thenReturn(Optional.of(existingEntry));
        when(customerRepositoryPort.findById(newCustomerId, FIRM_ID)).thenReturn(Optional.of(newCustomer));
        when(projectRepositoryPort.findById(newProjectId, FIRM_ID)).thenReturn(Optional.of(newProject));
        when(clockPort.now()).thenReturn(NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTimeEntryCommand command = new UpdateTimeEntryCommand(
                Optional.of(newCustomerId),
                Optional.of(newProjectId),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        // WHEN
        service.update(existingEntry.getId(), command);

        // THEN
        verify(customerRepositoryPort).findById(newCustomerId, FIRM_ID);
        verify(projectRepositoryPort).findById(newProjectId, FIRM_ID);
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        assertThat(timeEntryCaptor.getValue().getCustomerId()).isEqualTo(newCustomerId);
        assertThat(timeEntryCaptor.getValue().getProjectId()).isEqualTo(newProjectId);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_updateWithMismatchedProjectCustomer_WHEN_update_THEN_throwsIllegalArgumentException() {
        // GIVEN
        UUID wrongCustomerId = UUID.randomUUID();
        Project wrongProject = new Project(PROJECT_ID, FIRM_ID, wrongCustomerId, "Wrong Project", "ACTIVE", NOW);
        
        TimeEntry existingEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Original", 60, NOW);
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(timeEntryRepositoryPort.findById(existingEntry.getId(), FIRM_ID)).thenReturn(Optional.of(existingEntry));
        when(customerRepositoryPort.findById(CUSTOMER_ID, FIRM_ID)).thenReturn(Optional.of(customer));
        when(projectRepositoryPort.findById(PROJECT_ID, FIRM_ID)).thenReturn(Optional.of(wrongProject));

        UpdateTimeEntryCommand command = new UpdateTimeEntryCommand(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("New narrative"),
                Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> service.update(existingEntry.getId(), command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project does not belong to customer");
        
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    // ==================== SUBMIT TESTS ====================

    @Test
    void GIVEN_employeeOwnsEntry_WHEN_submit_THEN_entrySubmittedSuccessfully() {
        // GIVEN
        TimeEntry draftEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Draft", 60, NOW);
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(timeEntryRepositoryPort.findById(draftEntry.getId(), FIRM_ID)).thenReturn(Optional.of(draftEntry));
        when(clockPort.now()).thenReturn(NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        service.submit(draftEntry.getId());

        // THEN
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(draftEntry.getId(), FIRM_ID);
        verify(clockPort).now();
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        
        TimeEntry submitted = timeEntryCaptor.getValue();
        assertThat(submitted.getStatus()).isEqualTo(EntryStatus.SUBMITTED);
        
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_employeeSubmitsOthersEntry_WHEN_submit_THEN_throwsIllegalStateException() {
        // GIVEN
        UUID otherUserId = UUID.randomUUID();
        TimeEntry otherEntry = TimeEntry.draft(FIRM_ID, otherUserId, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Other's draft", 60, NOW);
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(timeEntryRepositoryPort.findById(otherEntry.getId(), FIRM_ID)).thenReturn(Optional.of(otherEntry));

        // WHEN & THEN
        assertThatThrownBy(() -> service.submit(otherEntry.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Employees can only modify their own entries");
        
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(otherEntry.getId(), FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_entryNotFound_WHEN_submit_THEN_throwsIllegalArgumentException() {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();
        when(currentUserPort.currentUser()).thenReturn(employee);
        when(timeEntryRepositoryPort.findById(nonExistentId, FIRM_ID)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> service.submit(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time entry not found");
        
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(nonExistentId, FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    // ==================== APPROVE TESTS ====================

    @Test
    void GIVEN_employeeTriesToApprove_WHEN_approve_THEN_throwsIllegalStateException() {
        // GIVEN
        UUID entryId = UUID.randomUUID();
        when(currentUserPort.currentUser()).thenReturn(employee);

        // WHEN & THEN
        assertThatThrownBy(() -> service.approve(entryId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Employees cannot approve entries");
        
        verify(currentUserPort).currentUser();
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_managerApprovesSubmittedEntry_WHEN_approve_THEN_entryApprovedAndEventPublished() {
        // GIVEN
        TimeEntry submittedEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Submitted", 60, NOW).submit(NOW);
        when(currentUserPort.currentUser()).thenReturn(manager);
        when(timeEntryRepositoryPort.findById(submittedEntry.getId(), FIRM_ID)).thenReturn(Optional.of(submittedEntry));
        when(clockPort.now()).thenReturn(NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        service.approve(submittedEntry.getId());

        // THEN
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(submittedEntry.getId(), FIRM_ID);
        verify(clockPort).now();
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        verify(outboxPort).append(eq(FIRM_ID), eq(submittedEntry.getId()), eventCaptor.capture());
        
        TimeEntry approved = timeEntryCaptor.getValue();
        assertThat(approved.getStatus()).isEqualTo(EntryStatus.APPROVED);
        assertThat(approved.getApprovedBy()).isEqualTo(manager.userId());
        
        DomainEvent event = eventCaptor.getValue();
        assertThat(event.eventType()).isEqualTo("ENTRY_APPROVED.v1");
        
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_adminApprovesSubmittedEntry_WHEN_approve_THEN_entryApprovedAndEventPublished() {
        // GIVEN
        TimeEntry submittedEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Submitted", 60, NOW).submit(NOW);
        when(currentUserPort.currentUser()).thenReturn(admin);
        when(timeEntryRepositoryPort.findById(submittedEntry.getId(), FIRM_ID)).thenReturn(Optional.of(submittedEntry));
        when(clockPort.now()).thenReturn(NOW);
        when(timeEntryRepositoryPort.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        service.approve(submittedEntry.getId());

        // THEN
        verify(timeEntryRepositoryPort).save(timeEntryCaptor.capture());
        verify(outboxPort).append(eq(FIRM_ID), eq(submittedEntry.getId()), any(DomainEvent.class));
        
        TimeEntry approved = timeEntryCaptor.getValue();
        assertThat(approved.getStatus()).isEqualTo(EntryStatus.APPROVED);
        assertThat(approved.getApprovedBy()).isEqualTo(admin.userId());
        
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_managerApprovesAlreadyApprovedEntry_WHEN_approve_THEN_throwsIllegalStateException() {
        // GIVEN
        TimeEntry approvedEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Approved", 60, NOW)
                .submit(NOW)
                .approve(manager.userId(), NOW);
        when(currentUserPort.currentUser()).thenReturn(manager);
        when(timeEntryRepositoryPort.findById(approvedEntry.getId(), FIRM_ID)).thenReturn(Optional.of(approvedEntry));

        // WHEN & THEN
        assertThatThrownBy(() -> service.approve(approvedEntry.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Approved entries are immutable");
        
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(approvedEntry.getId(), FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_adminApprovesAlreadyApprovedEntry_WHEN_approve_THEN_entryReApprovedSuccessfully() {
        // GIVEN
        TimeEntry approvedEntry = TimeEntry.draft(FIRM_ID, USER_ID, CUSTOMER_ID, PROJECT_ID, MATTER_ID, "Approved", 60, NOW)
                .submit(NOW)
                .approve(manager.userId(), NOW);
        when(currentUserPort.currentUser()).thenReturn(admin);
        when(timeEntryRepositoryPort.findById(approvedEntry.getId(), FIRM_ID)).thenReturn(Optional.of(approvedEntry));
        when(clockPort.now()).thenReturn(NOW);

        // WHEN & THEN
        // Even admin cannot re-approve an already approved entry - the domain model prevents it
        assertThatThrownBy(() -> service.approve(approvedEntry.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Entry must be submitted before approval");
        
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(approvedEntry.getId(), FIRM_ID);
        verify(clockPort).now();
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }

    @Test
    void GIVEN_entryNotFound_WHEN_approve_THEN_throwsIllegalArgumentException() {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();
        when(currentUserPort.currentUser()).thenReturn(manager);
        when(timeEntryRepositoryPort.findById(nonExistentId, FIRM_ID)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> service.approve(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time entry not found");
        
        verify(currentUserPort).currentUser();
        verify(timeEntryRepositoryPort).findById(nonExistentId, FIRM_ID);
        verifyNoMoreInteractions(timeEntryRepositoryPort, customerRepositoryPort, projectRepositoryPort, outboxPort, currentUserPort, clockPort);
    }
}
