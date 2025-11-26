package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.TimeEntryEntity;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;

/**
 * Tests for {@link TimeEntryMapper} ensuring complete mapping between domain and entity.
 */
class TimeEntryMapperTest {

    @Test
    void GIVEN_completeTimeEntry_WHEN_toEntity_THEN_allFieldsMapped() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID matterId = UUID.randomUUID();
        final UUID approvedBy = UUID.randomUUID();
        final String narrative = "Legal research for case XYZ";
        final int durationMinutes = 120;
        final EntryStatus status = EntryStatus.APPROVED;
        final Instant createdAt = Instant.parse("2025-11-20T10:00:00Z");
        final Instant updatedAt = Instant.parse("2025-11-20T15:00:00Z");
        final Instant approvedAt = Instant.parse("2025-11-21T09:00:00Z");

        final TimeEntry entry = TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(userId)
                .customerId(customerId)
                .projectId(projectId)
                .matterId(matterId)
                .narrative(narrative)
                .durationMinutes(durationMinutes)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .approvedBy(approvedBy)
                .approvedAt(approvedAt)
                .build();

        // WHEN
        final TimeEntryEntity entity = TimeEntryMapper.toEntity(entry);

        // THEN
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getFirmId()).isEqualTo(firmId);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getCustomerId()).isEqualTo(customerId);
        assertThat(entity.getProjectId()).isEqualTo(projectId);
        assertThat(entity.getMatterId()).isEqualTo(matterId);
        assertThat(entity.getNarrative()).isEqualTo(narrative);
        assertThat(entity.getDurationMinutes()).isEqualTo(durationMinutes);
        assertThat(entity.getStatus()).isEqualTo(status);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(entity.getApprovedBy()).isEqualTo(approvedBy);
        assertThat(entity.getApprovedAt()).isEqualTo(approvedAt);
    }

    @Test
    void GIVEN_timeEntryWithNullOptionalFields_WHEN_toEntity_THEN_nullFieldsPreserved() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final String narrative = "Meeting with client";
        final int durationMinutes = 60;
        final EntryStatus status = EntryStatus.DRAFT;
        final Instant createdAt = Instant.now();
        final Instant updatedAt = Instant.now();

        final TimeEntry entry = TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(userId)
                .customerId(customerId)
                .projectId(projectId)
                .matterId(null)
                .narrative(narrative)
                .durationMinutes(durationMinutes)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .approvedBy(null)
                .approvedAt(null)
                .build();

        // WHEN
        final TimeEntryEntity entity = TimeEntryMapper.toEntity(entry);

        // THEN
        assertThat(entity).isNotNull();
        assertThat(entity.getMatterId()).isNull();
        assertThat(entity.getApprovedBy()).isNull();
        assertThat(entity.getApprovedAt()).isNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getStatus()).isEqualTo(status);
    }

    @Test
    void GIVEN_draftStatus_WHEN_toEntity_THEN_statusMappedCorrectly() {
        // GIVEN
        final TimeEntry entry = TimeEntry.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .narrative("Draft entry")
                .durationMinutes(30)
                .status(EntryStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final TimeEntryEntity entity = TimeEntryMapper.toEntity(entry);

        // THEN
        assertThat(entity.getStatus()).isEqualTo(EntryStatus.DRAFT);
    }

    @Test
    void GIVEN_submittedStatus_WHEN_toEntity_THEN_statusMappedCorrectly() {
        // GIVEN
        final TimeEntry entry = TimeEntry.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .narrative("Submitted entry")
                .durationMinutes(45)
                .status(EntryStatus.SUBMITTED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final TimeEntryEntity entity = TimeEntryMapper.toEntity(entry);

        // THEN
        assertThat(entity.getStatus()).isEqualTo(EntryStatus.SUBMITTED);
    }

    @Test
    void GIVEN_completeTimeEntryEntity_WHEN_toDomain_THEN_allFieldsMapped() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID matterId = UUID.randomUUID();
        final UUID approvedBy = UUID.randomUUID();
        final String narrative = "Court appearance preparation";
        final int durationMinutes = 180;
        final EntryStatus status = EntryStatus.APPROVED;
        final Instant createdAt = Instant.parse("2025-11-22T08:00:00Z");
        final Instant updatedAt = Instant.parse("2025-11-22T12:00:00Z");
        final Instant approvedAt = Instant.parse("2025-11-22T14:00:00Z");

        final TimeEntryEntity entity = TimeEntryEntity.builder()
                .id(id)
                .firmId(firmId)
                .userId(userId)
                .customerId(customerId)
                .projectId(projectId)
                .matterId(matterId)
                .narrative(narrative)
                .durationMinutes(durationMinutes)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .approvedBy(approvedBy)
                .approvedAt(approvedAt)
                .build();

        // WHEN
        final TimeEntry domain = TimeEntryMapper.toDomain(entity);

        // THEN
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getFirmId()).isEqualTo(firmId);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getCustomerId()).isEqualTo(customerId);
        assertThat(domain.getProjectId()).isEqualTo(projectId);
        assertThat(domain.getMatterId()).isEqualTo(matterId);
        assertThat(domain.getNarrative()).isEqualTo(narrative);
        assertThat(domain.getDurationMinutes()).isEqualTo(durationMinutes);
        assertThat(domain.getStatus()).isEqualTo(status);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(domain.getApprovedBy()).isEqualTo(approvedBy);
        assertThat(domain.getApprovedAt()).isEqualTo(approvedAt);
    }

    @Test
    void GIVEN_entityWithNullOptionalFields_WHEN_toDomain_THEN_nullFieldsPreserved() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final String narrative = "Initial consultation";
        final int durationMinutes = 90;
        final EntryStatus status = EntryStatus.SUBMITTED;
        final Instant createdAt = Instant.now();
        final Instant updatedAt = Instant.now();

        final TimeEntryEntity entity = TimeEntryEntity.builder()
                .id(id)
                .firmId(firmId)
                .userId(userId)
                .customerId(customerId)
                .projectId(projectId)
                .matterId(null)
                .narrative(narrative)
                .durationMinutes(durationMinutes)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .approvedBy(null)
                .approvedAt(null)
                .build();

        // WHEN
        final TimeEntry domain = TimeEntryMapper.toDomain(entity);

        // THEN
        assertThat(domain).isNotNull();
        assertThat(domain.getMatterId()).isNull();
        assertThat(domain.getApprovedBy()).isNull();
        assertThat(domain.getApprovedAt()).isNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getStatus()).isEqualTo(status);
    }

    @Test
    void GIVEN_draftStatusEntity_WHEN_toDomain_THEN_statusMappedCorrectly() {
        // GIVEN
        final TimeEntryEntity entity = TimeEntryEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .narrative("Draft entity")
                .durationMinutes(25)
                .status(EntryStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final TimeEntry domain = TimeEntryMapper.toDomain(entity);

        // THEN
        assertThat(domain.getStatus()).isEqualTo(EntryStatus.DRAFT);
    }

    @Test
    void GIVEN_approvedStatusEntity_WHEN_toDomain_THEN_statusMappedCorrectly() {
        // GIVEN
        final TimeEntryEntity entity = TimeEntryEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .narrative("Approved entity")
                .durationMinutes(150)
                .status(EntryStatus.APPROVED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(UUID.randomUUID())
                .approvedAt(Instant.now())
                .build();

        // WHEN
        final TimeEntry domain = TimeEntryMapper.toDomain(entity);

        // THEN
        assertThat(domain.getStatus()).isEqualTo(EntryStatus.APPROVED);
    }

    @Test
    void GIVEN_zeroDuration_WHEN_toEntity_THEN_zeroPreserved() {
        // GIVEN
        final TimeEntry entry = TimeEntry.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .narrative("Zero duration test")
                .durationMinutes(0)
                .status(EntryStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final TimeEntryEntity entity = TimeEntryMapper.toEntity(entry);

        // THEN
        assertThat(entity.getDurationMinutes()).isZero();
    }

    @Test
    void GIVEN_largeDuration_WHEN_toEntity_THEN_valueMappedCorrectly() {
        // GIVEN
        final int largeDuration = Integer.MAX_VALUE;
        final TimeEntry entry = TimeEntry.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .narrative("Large duration test")
                .durationMinutes(largeDuration)
                .status(EntryStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final TimeEntryEntity entity = TimeEntryMapper.toEntity(entry);

        // THEN
        assertThat(entity.getDurationMinutes()).isEqualTo(largeDuration);
    }

    @Test
    void GIVEN_emptyNarrative_WHEN_toEntity_THEN_emptyStringPreserved() {
        // GIVEN
        final TimeEntry entry = TimeEntry.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .narrative("")
                .durationMinutes(30)
                .status(EntryStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final TimeEntryEntity entity = TimeEntryMapper.toEntity(entry);

        // THEN
        assertThat(entity.getNarrative()).isEmpty();
    }

    @Test
    void GIVEN_roundTripMapping_WHEN_toDomainAndToEntity_THEN_dataPreserved() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID matterId = UUID.randomUUID();
        final String narrative = "Round trip test";
        final int durationMinutes = 75;
        final EntryStatus status = EntryStatus.SUBMITTED;
        final Instant createdAt = Instant.now();
        final Instant updatedAt = Instant.now();

        final TimeEntry original = TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(userId)
                .customerId(customerId)
                .projectId(projectId)
                .matterId(matterId)
                .narrative(narrative)
                .durationMinutes(durationMinutes)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // WHEN
        final TimeEntryEntity entity = TimeEntryMapper.toEntity(original);
        final TimeEntry roundTrip = TimeEntryMapper.toDomain(entity);

        // THEN
        assertThat(roundTrip).isEqualTo(original);
    }
}
