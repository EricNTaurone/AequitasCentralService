package com.aequitas.aequitascentralservice.adapter.web;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.aequitas.aequitascentralservice.adapter.web.dto.PageResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.CreateTimeEntryRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.IdResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.TimeEntryResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateTimeEntryRequest;
import com.aequitas.aequitascentralservice.app.port.inbound.TimeEntryCommandPort;
import com.aequitas.aequitascentralservice.app.port.inbound.TimeEntryQueryPort;
import com.aequitas.aequitascentralservice.app.service.IdempotencyService;
import com.aequitas.aequitascentralservice.domain.command.CreateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.model.TimeEntryFilter;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;

@ExtendWith(MockitoExtension.class)
class TimeEntryControllerTest {

    private static final UUID ENTRY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID PROJECT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MATTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID FIRM_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final String IDEMPOTENCY_KEY = "key-123";
    private static final String NARRATIVE = "Draft motion";
    private static final String UPDATED_NARRATIVE = "Updated narrative";
    private static final int DURATION = 45;
    private static final int UPDATED_DURATION = 60;
    private static final String CURSOR = "cursor-1";

    @Mock
    private TimeEntryCommandPort commandPort;
    @Mock
    private TimeEntryQueryPort queryPort;
    @Mock
    private IdempotencyService idempotencyService;

    @Captor
    private ArgumentCaptor<CreateTimeEntryCommand> createCommandCaptor;
    @Captor
    private ArgumentCaptor<UpdateTimeEntryCommand> updateCommandCaptor;
    @Captor
    private ArgumentCaptor<Supplier<UUID>> supplierCaptor;
    @Captor
    private ArgumentCaptor<TimeEntryFilter> filterCaptor;
    @Captor
    private ArgumentCaptor<PageRequest> pageRequestCaptor;

    @InjectMocks
    private TimeEntryController controller;

    @Test
    void GIVEN_validCreateRequest_WHEN_create_THEN_returnsIdentifierAndPersists() {
        // GIVEN
        CreateTimeEntryRequest request = new CreateTimeEntryRequest(CUSTOMER_ID, PROJECT_ID, NARRATIVE, DURATION);
        request.setMatterId(MATTER_ID);
        when(commandPort.create(createCommandCaptor.capture())).thenReturn(ENTRY_ID);
        doAnswer(invocation -> {
            Supplier<UUID> supplier = invocation.getArgument(2);
            return supplier.get();
        })
                .when(idempotencyService)
                .execute(eq(IDEMPOTENCY_KEY), eq(IdempotencyOperation.TIME_ENTRY_CREATE),
                        org.mockito.ArgumentMatchers.any());

        // WHEN
        ResponseEntity<IdResponse> response = controller.create(IDEMPOTENCY_KEY, request);

        // THEN
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ENTRY_ID, response.getBody().getId());
        CreateTimeEntryCommand command = createCommandCaptor.getValue();
        assertEquals(CUSTOMER_ID, command.customerId());
        assertEquals(PROJECT_ID, command.projectId());
        assertEquals(MATTER_ID, command.matterId());
        assertEquals(NARRATIVE, command.narrative());
        assertEquals(DURATION, command.durationMinutes());
        verify(idempotencyService, times(1))
                .execute(eq(IDEMPOTENCY_KEY), eq(IdempotencyOperation.TIME_ENTRY_CREATE), supplierCaptor.capture());
        assertNotNull(supplierCaptor.getValue());
        verify(commandPort, times(1)).create(command);
        verifyNoMoreInteractions(commandPort, queryPort, idempotencyService);
    }

    @Test
    void GIVEN_patchPayload_WHEN_update_THEN_commandPortReceivesMappedCommand() {
        // GIVEN
        UpdateTimeEntryRequest request = new UpdateTimeEntryRequest();
        request.setCustomerId(CUSTOMER_ID);
        request.setProjectId(PROJECT_ID);
        request.setMatterId(MATTER_ID);
        request.setNarrative(UPDATED_NARRATIVE);
        request.setDurationMinutes(UPDATED_DURATION);

        // WHEN
        ResponseEntity<Void> response = controller.update(ENTRY_ID, request);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandPort, times(1)).update(eq(ENTRY_ID), updateCommandCaptor.capture());
        UpdateTimeEntryCommand command = updateCommandCaptor.getValue();
        assertEquals(Optional.of(CUSTOMER_ID), command.customerId());
        assertEquals(Optional.of(PROJECT_ID), command.projectId());
        assertEquals(Optional.of(MATTER_ID), command.matterId());
        assertEquals(Optional.of(UPDATED_NARRATIVE), command.narrative());
        assertEquals(Optional.of(UPDATED_DURATION), command.durationMinutes());
        verifyNoMoreInteractions(commandPort, queryPort, idempotencyService);
    }

    @Test
    void GIVEN_entryId_WHEN_submit_THEN_delegatesToCommandPort() {
        // GIVEN
        UUID id = ENTRY_ID;

        // WHEN
        ResponseEntity<Void> response = controller.submit(id);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandPort, times(1)).submit(id);
        verifyNoMoreInteractions(commandPort, queryPort, idempotencyService);
    }

    @Test
    void GIVEN_approvalRequest_WHEN_approve_THEN_idempotencyServiceAndCommandPortInvoked() {
        // GIVEN
        doAnswer(invocation -> {
            Supplier<UUID> supplier = invocation.getArgument(2);
            return supplier.get();
        })
                .when(idempotencyService)
                .execute(eq(IDEMPOTENCY_KEY), eq(IdempotencyOperation.TIME_ENTRY_APPROVE),
                        org.mockito.ArgumentMatchers.any());

        // WHEN
        ResponseEntity<Void> response = controller.approve(ENTRY_ID, IDEMPOTENCY_KEY);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(idempotencyService, times(1))
                .execute(eq(IDEMPOTENCY_KEY), eq(IdempotencyOperation.TIME_ENTRY_APPROVE), supplierCaptor.capture());
        assertNotNull(supplierCaptor.getValue());
        verify(commandPort, times(1)).approve(ENTRY_ID);
        verifyNoMoreInteractions(commandPort, queryPort, idempotencyService);
    }

    @Test
    void GIVEN_existingEntry_WHEN_findById_THEN_returnsResponseEntity() {
        // GIVEN
        TimeEntry entry = sampleEntry(EntryStatus.APPROVED);
        when(queryPort.findById(entry.id())).thenReturn(Optional.of(entry));

        // WHEN
        ResponseEntity<TimeEntryResponse> response = controller.findById(entry.id());

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(entry.id(), response.getBody().getId());
        verify(queryPort, times(1)).findById(entry.id());
        verifyNoMoreInteractions(commandPort, queryPort, idempotencyService);
    }

    @Test
    void GIVEN_missingEntry_WHEN_findById_THEN_returnsNotFound() {
        // GIVEN
        when(queryPort.findById(ENTRY_ID)).thenReturn(Optional.empty());

        // WHEN
        ResponseEntity<TimeEntryResponse> response = controller.findById(ENTRY_ID);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(queryPort, times(1)).findById(ENTRY_ID);
        verifyNoMoreInteractions(commandPort, queryPort, idempotencyService);
    }

    @Test
    void GIVEN_filters_WHEN_search_THEN_pageResponseReturnedAndArgumentsForwarded() {
        // GIVEN
        TimeEntry entry = sampleEntry(EntryStatus.SUBMITTED);
        PageResult<TimeEntry> page = new PageResult<>(List.of(entry), "cursor-2", 1, true);
        when(queryPort.search(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(page);

        // WHEN
        ResponseEntity<PageResponse<TimeEntryResponse>> response = controller.search(CUSTOMER_ID, PROJECT_ID,
                "submitted", USER_ID, 25, CURSOR);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().items().size());
        assertEquals("cursor-2", response.getBody().nextCursor());
        assertTrue(response.getBody().hasMore());
        verify(queryPort, times(1)).search(filterCaptor.capture(), pageRequestCaptor.capture());
        TimeEntryFilter filter = filterCaptor.getValue();
        assertEquals(Optional.of(CUSTOMER_ID), filter.customerId());
        assertEquals(Optional.of(PROJECT_ID), filter.projectId());
        assertEquals(Optional.of(EntryStatus.SUBMITTED), filter.status());
        assertEquals(Optional.of(USER_ID), filter.ownerId());
        PageRequest request = pageRequestCaptor.getValue();
        assertEquals(25, request.limit());
        assertEquals(CURSOR, request.cursor());
        verifyNoMoreInteractions(commandPort, queryPort, idempotencyService);
    }

    @Test
    void GIVEN_invalidStatus_WHEN_search_THEN_exceptionRaised() {
        // GIVEN
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.search(null, null, "bad", null, 10, null));

        // THEN
        assertEquals(
                "No enum constant com.aequitas.aequitascentralservice.domain.value.EntryStatus.BAD",
                exception.getMessage());
        verifyNoMoreInteractions(commandPort, queryPort, idempotencyService);
    }

    private TimeEntry sampleEntry(final EntryStatus status) {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        return TimeEntry.rehydrate(
                ENTRY_ID,
                FIRM_ID,
                USER_ID,
                CUSTOMER_ID,
                PROJECT_ID,
                MATTER_ID,
                NARRATIVE,
                DURATION,
                status,
                now,
                now,
                USER_ID,
                now);
    }
}
