package com.aequitas.aequitascentralservice.adapter.web;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.CreateTimeEntryRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.EntryStatus;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.TimeEntryResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateTimeEntryRequest;
import com.aequitas.aequitascentralservice.domain.command.CreateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;

/**
 * Unit tests for {@link TimeEntryDtoMapper} covering all mapping scenarios between REST DTOs and
 * domain commands/models.
 *
 * <p>These tests verify that the static utility methods correctly transform web-layer request/response
 * objects to/from domain objects, ensuring field mappings are accurate and complete.
 *
 * <p><strong>Testing Strategy:</strong> Each public mapping method has dedicated tests covering:
 * <ul>
 *   <li>Standard mapping scenarios with all fields populated</li>
 *   <li>Null/optional field handling</li>
 *   <li>Field value preservation and correctness</li>
 * </ul>
 *
 * <p><strong>Note:</strong> Since {@code TimeEntryDtoMapper} is annotated with {@code @UtilityClass},
 * all methods are static and no instance is created.
 */
class TimeEntryDtoMapperTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PROJECT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID MATTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID FIRM_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID ENTRY_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final String NARRATIVE = "Reviewed contract amendments";
    private static final int DURATION_MINUTES = 90;
    private static final Instant TIMESTAMP = Instant.parse("2024-11-15T10:30:00Z");

    @Test
    void GIVEN_validCreateTimeEntryRequest_WHEN_toCommand_THEN_returnsCreateTimeEntryCommandWithAllFieldsMapped() {
        // GIVEN
        final CreateTimeEntryRequest request =
                CreateTimeEntryRequest.builder()
                        .customerId(CUSTOMER_ID)
                        .projectId(PROJECT_ID)
                        .matterId(MATTER_ID)
                        .narrative(NARRATIVE)
                        .durationMinutes(DURATION_MINUTES)
                        .build();

        // WHEN
        final CreateTimeEntryCommand command = TimeEntryDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(command.projectId()).isEqualTo(PROJECT_ID);
        assertThat(command.matterId()).isEqualTo(MATTER_ID);
        assertThat(command.narrative()).isEqualTo(NARRATIVE);
        assertThat(command.durationMinutes()).isEqualTo(DURATION_MINUTES);
    }

    @Test
    void GIVEN_createTimeEntryRequestWithNullMatter_WHEN_toCommand_THEN_returnsCommandWithNullMatter() {
        // GIVEN
        final CreateTimeEntryRequest request =
                CreateTimeEntryRequest.builder()
                        .customerId(CUSTOMER_ID)
                        .projectId(PROJECT_ID)
                        .matterId(null)
                        .narrative(NARRATIVE)
                        .durationMinutes(DURATION_MINUTES)
                        .build();

        // WHEN
        final CreateTimeEntryCommand command = TimeEntryDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.matterId()).isNull();
    }

    @Test
    void GIVEN_validUpdateTimeEntryRequest_WHEN_toCommand_THEN_returnsUpdateTimeEntryCommandWithOptionalFields() {
        // GIVEN
        final UpdateTimeEntryRequest request =
                UpdateTimeEntryRequest.builder()
                        .customerId(CUSTOMER_ID)
                        .projectId(PROJECT_ID)
                        .matterId(MATTER_ID)
                        .narrative(NARRATIVE)
                        .durationMinutes(DURATION_MINUTES)
                        .build();

        // WHEN
        final UpdateTimeEntryCommand command = TimeEntryDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.customerId()).isEqualTo(Optional.of(CUSTOMER_ID));
        assertThat(command.projectId()).isEqualTo(Optional.of(PROJECT_ID));
        assertThat(command.matterId()).isEqualTo(Optional.of(MATTER_ID));
        assertThat(command.narrative()).isEqualTo(Optional.of(NARRATIVE));
        assertThat(command.durationMinutes()).isEqualTo(Optional.of(DURATION_MINUTES));
    }

    @Test
    void GIVEN_updateTimeEntryRequestWithNullFields_WHEN_toCommand_THEN_returnsCommandWithEmptyOptionals() {
        // GIVEN
        final UpdateTimeEntryRequest request =
                UpdateTimeEntryRequest.builder()
                        .customerId(null)
                        .projectId(null)
                        .matterId(null)
                        .narrative(null)
                        .durationMinutes(null)
                        .build();

        // WHEN
        final UpdateTimeEntryCommand command = TimeEntryDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.customerId()).isEmpty();
        assertThat(command.projectId()).isEmpty();
        assertThat(command.matterId()).isEmpty();
        assertThat(command.narrative()).isEmpty();
        assertThat(command.durationMinutes()).isEmpty();
    }

    @Test
    void GIVEN_updateTimeEntryRequestWithPartialFields_WHEN_toCommand_THEN_returnsCommandWithMixedOptionals() {
        // GIVEN
        final UpdateTimeEntryRequest request =
                UpdateTimeEntryRequest.builder()
                        .customerId(CUSTOMER_ID)
                        .projectId(null)
                        .matterId(MATTER_ID)
                        .narrative(null)
                        .durationMinutes(DURATION_MINUTES)
                        .build();

        // WHEN
        final UpdateTimeEntryCommand command = TimeEntryDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.customerId()).isEqualTo(Optional.of(CUSTOMER_ID));
        assertThat(command.projectId()).isEmpty();
        assertThat(command.matterId()).isEqualTo(Optional.of(MATTER_ID));
        assertThat(command.narrative()).isEmpty();
        assertThat(command.durationMinutes()).isEqualTo(Optional.of(DURATION_MINUTES));
    }

    @Test
    void GIVEN_draftTimeEntry_WHEN_toResponse_THEN_returnsTimeEntryResponseWithAllFieldsMapped() {
        // GIVEN
        final TimeEntry entry =
                TimeEntry.rehydrate(
                        ENTRY_ID,
                        FIRM_ID,
                        USER_ID,
                        CUSTOMER_ID,
                        PROJECT_ID,
                        MATTER_ID,
                        NARRATIVE,
                        DURATION_MINUTES,
                        com.aequitas.aequitascentralservice.domain.value.EntryStatus.DRAFT,
                        TIMESTAMP,
                        TIMESTAMP,
                        null,
                        null);

        // WHEN
        final TimeEntryResponse response = TimeEntryDtoMapper.toResponse(entry);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(ENTRY_ID);
        assertThat(response.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(response.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(response.getMatterId()).isEqualTo(MATTER_ID);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getNarrative()).isEqualTo(NARRATIVE);
        assertThat(response.getDurationMinutes()).isEqualTo(DURATION_MINUTES);
        assertThat(response.getStatus()).isEqualTo(EntryStatus.DRAFT);
        assertThat(response.getCreatedAt()).isEqualTo(TIMESTAMP);
        assertThat(response.getUpdatedAt()).isEqualTo(TIMESTAMP);
        assertThat(response.getApprovedAt()).isNull();
    }

    @Test
    void GIVEN_submittedTimeEntry_WHEN_toResponse_THEN_returnsResponseWithSubmittedStatus() {
        // GIVEN
        final TimeEntry entry =
                TimeEntry.rehydrate(
                        ENTRY_ID,
                        FIRM_ID,
                        USER_ID,
                        CUSTOMER_ID,
                        PROJECT_ID,
                        MATTER_ID,
                        NARRATIVE,
                        DURATION_MINUTES,
                        com.aequitas.aequitascentralservice.domain.value.EntryStatus.SUBMITTED,
                        TIMESTAMP,
                        TIMESTAMP,
                        null,
                        null);

        // WHEN
        final TimeEntryResponse response = TimeEntryDtoMapper.toResponse(entry);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(EntryStatus.SUBMITTED);
    }

    @Test
    void GIVEN_approvedTimeEntryWithApprovedAt_WHEN_toResponse_THEN_returnsResponseWithApprovalTimestamp() {
        // GIVEN
        final Instant approvedAt = TIMESTAMP.plusSeconds(3600);
        final TimeEntry entry =
                TimeEntry.rehydrate(
                        ENTRY_ID,
                        FIRM_ID,
                        USER_ID,
                        CUSTOMER_ID,
                        PROJECT_ID,
                        MATTER_ID,
                        NARRATIVE,
                        DURATION_MINUTES,
                        com.aequitas.aequitascentralservice.domain.value.EntryStatus.APPROVED,
                        TIMESTAMP,
                        TIMESTAMP,
                        USER_ID,
                        approvedAt);

        // WHEN
        final TimeEntryResponse response = TimeEntryDtoMapper.toResponse(entry);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(EntryStatus.APPROVED);
        assertThat(response.getApprovedAt()).isEqualTo(approvedAt);
    }

    @Test
    void GIVEN_timeEntryWithNullMatter_WHEN_toResponse_THEN_returnsResponseWithNullMatter() {
        // GIVEN
        final TimeEntry entry =
                TimeEntry.rehydrate(
                        ENTRY_ID,
                        FIRM_ID,
                        USER_ID,
                        CUSTOMER_ID,
                        PROJECT_ID,
                        null,
                        NARRATIVE,
                        DURATION_MINUTES,
                        com.aequitas.aequitascentralservice.domain.value.EntryStatus.DRAFT,
                        TIMESTAMP,
                        TIMESTAMP,
                        null,
                        null);

        // WHEN
        final TimeEntryResponse response = TimeEntryDtoMapper.toResponse(entry);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getMatterId()).isNull();
    }
}
