package com.aequitas.aequitascentralservice.domain.model;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.domain.value.EntryStatus;

/**
 * Unit tests covering invariants on the {@link TimeEntry} aggregate.
 * Tests follow GIVEN_WHEN_THEN naming convention with comprehensive coverage.
 */
class TimeEntryTest {

    // ====================== draft() Factory Tests ======================

    @Test
    void GIVEN_validParameters_WHEN_draft_THEN_createsEntryWithDraftStatus() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID matterId = UUID.randomUUID();
        final Instant now = Instant.now();

        // WHEN
        final TimeEntry entry = TimeEntry.draft(
                firmId, userId, customerId, projectId, matterId, "Review contract", 60, now);

        // THEN
        assertThat(entry.getId()).isNotNull();
        assertThat(entry.getFirmId()).isEqualTo(firmId);
        assertThat(entry.getUserId()).isEqualTo(userId);
        assertThat(entry.getCustomerId()).isEqualTo(customerId);
        assertThat(entry.getProjectId()).isEqualTo(projectId);
        assertThat(entry.getMatterId()).isEqualTo(matterId);
        assertThat(entry.getNarrative()).isEqualTo("Review contract");
        assertThat(entry.getDurationMinutes()).isEqualTo(60);
        assertThat(entry.getStatus()).isEqualTo(EntryStatus.DRAFT);
        assertThat(entry.getCreatedAt()).isEqualTo(now);
        assertThat(entry.getUpdatedAt()).isEqualTo(now);
        assertThat(entry.getApprovedBy()).isNull();
        assertThat(entry.getApprovedAt()).isNull();
    }

    @Test
    void GIVEN_nullMatterId_WHEN_draft_THEN_createsEntryWithNullMatter() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final Instant now = Instant.now();

        // WHEN
        final TimeEntry entry = TimeEntry.draft(
                firmId, userId, customerId, projectId, null, "Review contract", 60, now);

        // THEN
        assertThat(entry.getMatterId()).isNull();
        assertThat(entry.getStatus()).isEqualTo(EntryStatus.DRAFT);
    }

    @Test
    void GIVEN_negativeDuration_WHEN_draft_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final int negativeDuration = -5;

        // WHEN & THEN
        assertThatThrownBy(() ->
                TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Review",
                        negativeDuration,
                        Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration must be positive");
    }

    @Test
    void GIVEN_zeroDuration_WHEN_draft_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final int zeroDuration = 0;

        // WHEN & THEN
        assertThatThrownBy(() ->
                TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Review",
                        zeroDuration,
                        Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration must be positive");
    }

    @Test
    void GIVEN_durationExceeds24Hours_WHEN_draft_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final int excessiveDuration = (24 * 60) + 1; // 1441 minutes

        // WHEN & THEN
        assertThatThrownBy(() ->
                TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Review",
                        excessiveDuration,
                        Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration exceeds 24 hours");
    }

    @Test
    void GIVEN_nullNarrative_WHEN_draft_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final String nullNarrative = null;

        // WHEN & THEN
        assertThatThrownBy(() ->
                TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        nullNarrative,
                        60,
                        Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Narrative is required");
    }

    @Test
    void GIVEN_blankNarrative_WHEN_draft_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final String blankNarrative = "   ";

        // WHEN & THEN
        assertThatThrownBy(() ->
                TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        blankNarrative,
                        60,
                        Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Narrative is required");
    }

    @Test
    void GIVEN_narrativeExceeds2048Characters_WHEN_draft_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final String excessiveNarrative = "x".repeat(2049);

        // WHEN & THEN
        assertThatThrownBy(() ->
                TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        excessiveNarrative,
                        60,
                        Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Narrative exceeds 2048 characters");
    }

    @Test
    void GIVEN_narrativeExactly2048Characters_WHEN_draft_THEN_createsEntry() {
        // GIVEN
        final String boundaryNarrative = "x".repeat(2048);

        // WHEN
        final TimeEntry entry = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                boundaryNarrative,
                60,
                Instant.now());

        // THEN
        assertThat(entry.getNarrative()).hasSize(2048);
        assertThat(entry.getStatus()).isEqualTo(EntryStatus.DRAFT);
    }

    @Test
    void GIVEN_boundaryDuration1Minute_WHEN_draft_THEN_createsEntry() {
        // GIVEN
        final int minimumDuration = 1;

        // WHEN
        final TimeEntry entry = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Quick review",
                minimumDuration,
                Instant.now());

        // THEN
        assertThat(entry.getDurationMinutes()).isEqualTo(1);
        assertThat(entry.getStatus()).isEqualTo(EntryStatus.DRAFT);
    }

    @Test
    void GIVEN_boundaryDuration24Hours_WHEN_draft_THEN_createsEntry() {
        // GIVEN
        final int maximumDuration = 24 * 60; // 1440 minutes

        // WHEN
        final TimeEntry entry = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Full day work",
                maximumDuration,
                Instant.now());

        // THEN
        assertThat(entry.getDurationMinutes()).isEqualTo(1440);
        assertThat(entry.getStatus()).isEqualTo(EntryStatus.DRAFT);
    }

    // ====================== rehydrate() Tests ======================

    @Test
    void GIVEN_completeAggregateData_WHEN_rehydrate_THEN_restoresAllFields() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID matterId = UUID.randomUUID();
        final UUID approvedBy = UUID.randomUUID();
        final Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
        final Instant updatedAt = Instant.parse("2025-01-01T11:00:00Z");
        final Instant approvedAt = Instant.parse("2025-01-01T12:00:00Z");

        // WHEN
        final TimeEntry entry = TimeEntry.rehydrate(
                id, firmId, userId, customerId, projectId, matterId,
                "Approved work", 120, EntryStatus.APPROVED,
                createdAt, updatedAt, approvedBy, approvedAt);

        // THEN
        assertThat(entry.getId()).isEqualTo(id);
        assertThat(entry.getFirmId()).isEqualTo(firmId);
        assertThat(entry.getUserId()).isEqualTo(userId);
        assertThat(entry.getCustomerId()).isEqualTo(customerId);
        assertThat(entry.getProjectId()).isEqualTo(projectId);
        assertThat(entry.getMatterId()).isEqualTo(matterId);
        assertThat(entry.getNarrative()).isEqualTo("Approved work");
        assertThat(entry.getDurationMinutes()).isEqualTo(120);
        assertThat(entry.getStatus()).isEqualTo(EntryStatus.APPROVED);
        assertThat(entry.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entry.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(entry.getApprovedBy()).isEqualTo(approvedBy);
        assertThat(entry.getApprovedAt()).isEqualTo(approvedAt);
    }

    @Test
    void GIVEN_nullOptionalFields_WHEN_rehydrate_THEN_restoresWithNulls() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
        final Instant updatedAt = Instant.parse("2025-01-01T11:00:00Z");

        // WHEN
        final TimeEntry entry = TimeEntry.rehydrate(
                id, firmId, userId, customerId, projectId, null,
                "Draft work", 90, EntryStatus.DRAFT,
                createdAt, updatedAt, null, null);

        // THEN
        assertThat(entry.getMatterId()).isNull();
        assertThat(entry.getApprovedBy()).isNull();
        assertThat(entry.getApprovedAt()).isNull();
        assertThat(entry.getStatus()).isEqualTo(EntryStatus.DRAFT);
    }

    // ====================== submit() Tests ======================

    @Test
    void GIVEN_draftEntry_WHEN_submit_THEN_transitionsToSubmitted() {
        // GIVEN
        final TimeEntry draft = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Draft work",
                30,
                Instant.parse("2025-01-01T10:00:00Z"));
        final Instant submitTime = Instant.parse("2025-01-01T11:00:00Z");

        // WHEN
        final TimeEntry submitted = draft.submit(submitTime);

        // THEN
        assertThat(submitted.getStatus()).isEqualTo(EntryStatus.SUBMITTED);
        assertThat(submitted.getUpdatedAt()).isEqualTo(submitTime);
        assertThat(submitted.getId()).isEqualTo(draft.getId());
        assertThat(submitted.getCreatedAt()).isEqualTo(draft.getCreatedAt());
        assertThat(submitted.getNarrative()).isEqualTo(draft.getNarrative());
    }

    @Test
    void GIVEN_submittedEntry_WHEN_submit_THEN_throwsIllegalStateException() {
        // GIVEN
        final TimeEntry submitted = TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Draft",
                        30,
                        Instant.now())
                .submit(Instant.now());

        // WHEN & THEN
        assertThatThrownBy(() -> submitted.submit(Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only draft entries can be submitted");
    }

    @Test
    void GIVEN_approvedEntry_WHEN_submit_THEN_throwsIllegalStateException() {
        // GIVEN
        final TimeEntry approved = TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Draft",
                        30,
                        Instant.now())
                .submit(Instant.now())
                .approve(UUID.randomUUID(), Instant.now());

        // WHEN & THEN
        assertThatThrownBy(() -> approved.submit(Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only draft entries can be submitted");
    }

    // ====================== approve() Tests ======================

    @Test
    void GIVEN_submittedEntry_WHEN_approve_THEN_transitionsToApproved() {
        // GIVEN
        final TimeEntry submitted = TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Submitted work",
                        45,
                        Instant.parse("2025-01-01T10:00:00Z"))
                .submit(Instant.parse("2025-01-01T11:00:00Z"));
        final UUID approverId = UUID.randomUUID();
        final Instant approveTime = Instant.parse("2025-01-01T12:00:00Z");

        // WHEN
        final TimeEntry approved = submitted.approve(approverId, approveTime);

        // THEN
        assertThat(approved.getStatus()).isEqualTo(EntryStatus.APPROVED);
        assertThat(approved.getApprovedBy()).isEqualTo(approverId);
        assertThat(approved.getApprovedAt()).isEqualTo(approveTime);
        assertThat(approved.getUpdatedAt()).isEqualTo(approveTime);
        assertThat(approved.getId()).isEqualTo(submitted.getId());
        assertThat(approved.getCreatedAt()).isEqualTo(submitted.getCreatedAt());
    }

    @Test
    void GIVEN_draftEntry_WHEN_approve_THEN_throwsIllegalStateException() {
        // GIVEN
        final TimeEntry draft = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Draft",
                30,
                Instant.now());

        // WHEN & THEN
        assertThatThrownBy(() -> draft.approve(UUID.randomUUID(), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Entry must be submitted before approval");
    }

    @Test
    void GIVEN_alreadyApprovedEntry_WHEN_approve_THEN_throwsIllegalStateException() {
        // GIVEN
        final TimeEntry approved = TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Draft",
                        30,
                        Instant.now())
                .submit(Instant.now())
                .approve(UUID.randomUUID(), Instant.now());

        // WHEN & THEN
        assertThatThrownBy(() -> approved.approve(UUID.randomUUID(), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Entry must be submitted before approval");
    }

    @Test
    void GIVEN_nullApproverId_WHEN_approve_THEN_throwsNullPointerException() {
        // GIVEN
        final TimeEntry submitted = TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Submitted",
                        30,
                        Instant.now())
                .submit(Instant.now());

        // WHEN & THEN
        assertThatThrownBy(() -> submitted.approve(null, Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    // ====================== updateDetails() Tests ======================

    @Test
    void GIVEN_draftEntry_WHEN_updateDetails_THEN_returnsNewSnapshotWithUpdatedFields() {
        // GIVEN
        final TimeEntry original = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Original narrative",
                60,
                Instant.parse("2025-01-01T10:00:00Z"));
        final UUID newCustomerId = UUID.randomUUID();
        final UUID newProjectId = UUID.randomUUID();
        final UUID newMatterId = UUID.randomUUID();
        final Instant updateTime = Instant.parse("2025-01-01T11:00:00Z");

        // WHEN
        final TimeEntry updated = original.updateDetails(
                "Updated narrative",
                newCustomerId,
                newProjectId,
                newMatterId,
                90,
                updateTime);

        // THEN
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getFirmId()).isEqualTo(original.getFirmId());
        assertThat(updated.getUserId()).isEqualTo(original.getUserId());
        assertThat(updated.getCustomerId()).isEqualTo(newCustomerId);
        assertThat(updated.getProjectId()).isEqualTo(newProjectId);
        assertThat(updated.getMatterId()).isEqualTo(newMatterId);
        assertThat(updated.getNarrative()).isEqualTo("Updated narrative");
        assertThat(updated.getDurationMinutes()).isEqualTo(90);
        assertThat(updated.getStatus()).isEqualTo(original.getStatus());
        assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isEqualTo(updateTime);
    }

    @Test
    void GIVEN_invalidDuration_WHEN_updateDetails_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final TimeEntry entry = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Original",
                60,
                Instant.now());

        // WHEN & THEN
        assertThatThrownBy(() -> entry.updateDetails(
                "Updated",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                -10,
                Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration must be positive");
    }

    @Test
    void GIVEN_invalidNarrative_WHEN_updateDetails_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final TimeEntry entry = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Original",
                60,
                Instant.now());

        // WHEN & THEN
        assertThatThrownBy(() -> entry.updateDetails(
                "",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                60,
                Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Narrative is required");
    }

    @Test
    void GIVEN_approvedEntry_WHEN_updateDetails_THEN_updatesFieldsButPreservesApprovalStatus() {
        // GIVEN
        final TimeEntry approved = TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Original",
                        60,
                        Instant.now())
                .submit(Instant.now())
                .approve(UUID.randomUUID(), Instant.now());

        // WHEN
        final TimeEntry updated = approved.updateDetails(
                "Updated after approval",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                75,
                Instant.now());

        // THEN
        assertThat(updated.getStatus()).isEqualTo(EntryStatus.APPROVED);
        assertThat(updated.getApprovedBy()).isEqualTo(approved.getApprovedBy());
        assertThat(updated.getApprovedAt()).isEqualTo(approved.getApprovedAt());
        assertThat(updated.getNarrative()).isEqualTo("Updated after approval");
        assertThat(updated.getDurationMinutes()).isEqualTo(75);
    }

    // ====================== Immutability Tests ======================

    @Test
    void GIVEN_timeEntry_WHEN_submit_THEN_originalRemainsUnchanged() {
        // GIVEN
        final TimeEntry original = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Original",
                60,
                Instant.now());

        // WHEN
        final TimeEntry submitted = original.submit(Instant.now());

        // THEN
        assertThat(original.getStatus()).isEqualTo(EntryStatus.DRAFT);
        assertThat(submitted.getStatus()).isEqualTo(EntryStatus.SUBMITTED);
    }

    @Test
    void GIVEN_timeEntry_WHEN_updateDetails_THEN_originalRemainsUnchanged() {
        // GIVEN
        final TimeEntry original = TimeEntry.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Original narrative",
                60,
                Instant.now());
        final String originalNarrative = original.getNarrative();
        final int originalDuration = original.getDurationMinutes();

        // WHEN
        final TimeEntry updated = original.updateDetails(
                "Updated narrative",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                120,
                Instant.now());

        // THEN
        assertThat(original.getNarrative()).isEqualTo(originalNarrative);
        assertThat(original.getDurationMinutes()).isEqualTo(originalDuration);
        assertThat(updated.getNarrative()).isEqualTo("Updated narrative");
        assertThat(updated.getDurationMinutes()).isEqualTo(120);
    }
}
