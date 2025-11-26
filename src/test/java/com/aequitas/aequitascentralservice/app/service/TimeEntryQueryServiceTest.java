package com.aequitas.aequitascentralservice.app.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.TimeEntryRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.model.TimeEntryFilter;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import com.aequitas.aequitascentralservice.domain.value.Role;

@ExtendWith(MockitoExtension.class)
class TimeEntryQueryServiceTest {

    @Mock
    private TimeEntryRepositoryPort repositoryPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @Captor
    private ArgumentCaptor<TimeEntryFilter> filterCaptor;

    private TimeEntryQueryService service;

    private UUID firmId;
    private UUID userId;
    private UUID otherUserId;
    private UUID entryId;
    private UUID customerId;
    private UUID projectId;
    private UUID matterId;

    @BeforeEach
    void setUp() {
        service = new TimeEntryQueryService(repositoryPort, currentUserPort);
        
        firmId = UUID.randomUUID();
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        entryId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        matterId = UUID.randomUUID();
    }

    // ========== findById Tests ==========

    @Test
    void GIVEN_adminUser_WHEN_findById_THEN_returnsEntry() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.ADMIN);
        final TimeEntry entry = createTimeEntry(entryId, userId);
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(entryId, firmId)).thenReturn(Optional.of(entry));

        // WHEN
        final Optional<TimeEntry> result = service.findById(entryId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(entry);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).findById(entryId, firmId);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_managerUser_WHEN_findById_THEN_returnsEntry() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.MANAGER);
        final TimeEntry entry = createTimeEntry(entryId, otherUserId);
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(entryId, firmId)).thenReturn(Optional.of(entry));

        // WHEN
        final Optional<TimeEntry> result = service.findById(entryId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(entry);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).findById(entryId, firmId);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_employeeUserOwnsEntry_WHEN_findById_THEN_returnsEntry() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.EMPLOYEE);
        final TimeEntry entry = createTimeEntry(entryId, userId);
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(entryId, firmId)).thenReturn(Optional.of(entry));

        // WHEN
        final Optional<TimeEntry> result = service.findById(entryId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(entry);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).findById(entryId, firmId);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_employeeUserDoesNotOwnEntry_WHEN_findById_THEN_returnsEmpty() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.EMPLOYEE);
        final TimeEntry entry = createTimeEntry(entryId, otherUserId);
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(entryId, firmId)).thenReturn(Optional.of(entry));

        // WHEN
        final Optional<TimeEntry> result = service.findById(entryId);

        // THEN
        assertThat(result).isEmpty();
        verify(currentUserPort).currentUser();
        verify(repositoryPort).findById(entryId, firmId);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_entryNotFound_WHEN_findById_THEN_returnsEmpty() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.ADMIN);
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(entryId, firmId)).thenReturn(Optional.empty());

        // WHEN
        final Optional<TimeEntry> result = service.findById(entryId);

        // THEN
        assertThat(result).isEmpty();
        verify(currentUserPort).currentUser();
        verify(repositoryPort).findById(entryId, firmId);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    // ========== search Tests ==========

    @Test
    void GIVEN_adminUser_WHEN_search_THEN_searchesWithOriginalFilter() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.ADMIN);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.of(customerId),
                Optional.of(projectId),
                Optional.of(EntryStatus.DRAFT),
                Optional.empty()
        );
        final PageRequest pageRequest = new PageRequest(10, null);
        final TimeEntry entry = createTimeEntry(entryId, userId);
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(entry), null, 1L, false
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        assertThat(result.items()).hasSize(1);
        assertThat(result.totalItems()).isEqualTo(1L);
        assertThat(result.hasMore()).isFalse();
        
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        
        final TimeEntryFilter capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.customerId()).isEqualTo(Optional.of(customerId));
        assertThat(capturedFilter.projectId()).isEqualTo(Optional.of(projectId));
        assertThat(capturedFilter.status()).isEqualTo(Optional.of(EntryStatus.DRAFT));
        assertThat(capturedFilter.ownerId()).isEmpty();
        
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_managerUser_WHEN_search_THEN_searchesWithOriginalFilter() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.MANAGER);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.empty(),
                Optional.of(projectId),
                Optional.empty(),
                Optional.of(otherUserId)
        );
        final PageRequest pageRequest = new PageRequest(20, "cursor123");
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(), "nextCursor", 0L, false
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        assertThat(result.items()).isEmpty();
        
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        
        final TimeEntryFilter capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.projectId()).isEqualTo(Optional.of(projectId));
        assertThat(capturedFilter.ownerId()).isEqualTo(Optional.of(otherUserId));
        
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_employeeUser_WHEN_search_THEN_enrichesFilterWithOwnerId() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.EMPLOYEE);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.of(customerId),
                Optional.empty(),
                Optional.of(EntryStatus.SUBMITTED),
                Optional.empty()
        );
        final PageRequest pageRequest = new PageRequest(5, null);
        final TimeEntry entry = createTimeEntry(entryId, userId);
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(entry), null, 1L, false
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        
        final TimeEntryFilter capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.customerId()).isEqualTo(Optional.of(customerId));
        assertThat(capturedFilter.projectId()).isEmpty();
        assertThat(capturedFilter.status()).isEqualTo(Optional.of(EntryStatus.SUBMITTED));
        assertThat(capturedFilter.ownerId()).isEqualTo(Optional.of(userId));
        
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_employeeUserWithOwnerIdInFilter_WHEN_search_THEN_overridesOwnerIdWithCurrentUserId() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.EMPLOYEE);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(otherUserId) // Employee tries to search for another user's entries
        );
        final PageRequest pageRequest = new PageRequest(10, null);
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(), null, 0L, false
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        
        final TimeEntryFilter capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.ownerId()).isEqualTo(Optional.of(userId));
        
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_adminUserWithEmptyFilter_WHEN_search_THEN_searchesWithEmptyFilter() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.ADMIN);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        final PageRequest pageRequest = new PageRequest(50, null);
        final TimeEntry entry1 = createTimeEntry(UUID.randomUUID(), userId);
        final TimeEntry entry2 = createTimeEntry(UUID.randomUUID(), otherUserId);
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(entry1, entry2), "nextCursor", 2L, true
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        assertThat(result.items()).hasSize(2);
        assertThat(result.hasMore()).isTrue();
        
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        
        final TimeEntryFilter capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.customerId()).isEmpty();
        assertThat(capturedFilter.projectId()).isEmpty();
        assertThat(capturedFilter.status()).isEmpty();
        assertThat(capturedFilter.ownerId()).isEmpty();
        
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_employeeUserWithEmptyFilter_WHEN_search_THEN_enrichesWithOwnerId() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.EMPLOYEE);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        final PageRequest pageRequest = new PageRequest(10, null);
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(), null, 0L, false
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        
        final TimeEntryFilter capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.ownerId()).isEqualTo(Optional.of(userId));
        
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_draftStatus_WHEN_search_THEN_filtersByDraftStatus() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.ADMIN);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.empty(), Optional.empty(), Optional.of(EntryStatus.DRAFT), Optional.empty()
        );
        final PageRequest pageRequest = new PageRequest(10, null);
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(), null, 0L, false
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        assertThat(filterCaptor.getValue().status()).isEqualTo(Optional.of(EntryStatus.DRAFT));
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_submittedStatus_WHEN_search_THEN_filtersBySubmittedStatus() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.ADMIN);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.empty(), Optional.empty(), Optional.of(EntryStatus.SUBMITTED), Optional.empty()
        );
        final PageRequest pageRequest = new PageRequest(10, null);
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(), null, 0L, false
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        assertThat(filterCaptor.getValue().status()).isEqualTo(Optional.of(EntryStatus.SUBMITTED));
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_approvedStatus_WHEN_search_THEN_filtersByApprovedStatus() {
        // GIVEN
        final CurrentUser currentUser = new CurrentUser(userId, firmId, Role.ADMIN);
        final TimeEntryFilter filter = new TimeEntryFilter(
                Optional.empty(), Optional.empty(), Optional.of(EntryStatus.APPROVED), Optional.empty()
        );
        final PageRequest pageRequest = new PageRequest(10, null);
        final PageResult<TimeEntry> pageResult = new PageResult<>(
                List.of(), null, 0L, false
        );
        
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.search(eq(firmId), any(TimeEntryFilter.class), eq(pageRequest)))
                .thenReturn(pageResult);

        // WHEN
        final PageResult<TimeEntry> result = service.search(filter, pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).search(eq(firmId), filterCaptor.capture(), eq(pageRequest));
        assertThat(filterCaptor.getValue().status()).isEqualTo(Optional.of(EntryStatus.APPROVED));
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    // ========== Helper Methods ==========

    private TimeEntry createTimeEntry(final UUID id, final UUID ownerId) {
        return TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(ownerId)
                .customerId(customerId)
                .projectId(projectId)
                .matterId(matterId)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(EntryStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }
}
