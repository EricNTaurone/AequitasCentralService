package com.aequitas.aequitascentralservice.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests covering invariants on the {@link TimeEntry} aggregate.
 */
class TimeEntryTest {

    @Test
    @DisplayName("draft should create immutable snapshot with DRAFT status")
    void draftCreatesEntry() {
        final UUID firmId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID matterId = UUID.randomUUID();
        final Instant now = Instant.now();

        final TimeEntry entry =
                TimeEntry.draft(
                        firmId, userId, customerId, projectId, matterId, "Review contract", 60, now);

        assertThat(entry.status()).isEqualTo(EntryStatus.DRAFT);
        assertThat(entry.durationMinutes()).isEqualTo(60);
        assertThat(entry.createdAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("draft should reject negative durations")
    void draftRejectsInvalidDuration() {
        assertThatThrownBy(
                        () ->
                                TimeEntry.draft(
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        null,
                                        "Review",
                                        -5,
                                        Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("submit should only allow DRAFT entries")
    void submitTransitions() {
        final TimeEntry entry =
                TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Draft",
                        30,
                        Instant.now());

        assertThat(entry.submit(Instant.now()).status()).isEqualTo(EntryStatus.SUBMITTED);
        final TimeEntry approved = entry.submit(Instant.now());
        assertThatThrownBy(() -> approved.submit(Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("approve should require SUBMITTED state")
    void approveValidations() {
        final TimeEntry entry =
                TimeEntry.draft(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                null,
                                "Draft",
                                30,
                                Instant.now())
                        .submit(Instant.now());

        assertThat(entry.approve(UUID.randomUUID(), Instant.now()).status())
                .isEqualTo(EntryStatus.APPROVED);
        final TimeEntry draft =
                TimeEntry.draft(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Draft",
                        30,
                        Instant.now());
        assertThatThrownBy(() -> draft.approve(UUID.randomUUID(), Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }
}
