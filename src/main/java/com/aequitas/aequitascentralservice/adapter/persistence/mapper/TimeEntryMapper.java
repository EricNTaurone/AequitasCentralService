package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.TimeEntryEntity;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;

/**
 * Mapper bridging {@link TimeEntry} aggregates and {@link TimeEntryEntity} rows.
 */
public final class TimeEntryMapper {

    private TimeEntryMapper() {}

    /**
     * Converts a domain aggregate into a managed entity.
     *
     * @param entry aggregate.
     * @return entity snapshot.
     */
    public static TimeEntryEntity toEntity(final TimeEntry entry) {
        final TimeEntryEntity entity = new TimeEntryEntity();
        entity.setId(entry.id());
        entity.setFirmId(entry.firmId());
        entity.setUserId(entry.userId());
        entity.setCustomerId(entry.customerId());
        entity.setProjectId(entry.projectId());
        entity.setMatterId(entry.matterId());
        entity.setNarrative(entry.narrative());
        entity.setDurationMinutes(entry.durationMinutes());
        entity.setStatus(entry.status());
        entity.setCreatedAt(entry.createdAt());
        entity.setUpdatedAt(entry.updatedAt());
        entity.setApprovedAt(entry.approvedAt());
        entity.setApprovedBy(entry.approvedBy());
        return entity;
    }

    /**
     * Converts a persistence entity into a domain aggregate.
     *
     * @param entity managed entity.
     * @return aggregate snapshot.
     */
    public static TimeEntry toDomain(final TimeEntryEntity entity) {
        return TimeEntry.rehydrate(
                entity.getId(),
                entity.getFirmId(),
                entity.getUserId(),
                entity.getCustomerId(),
                entity.getProjectId(),
                entity.getMatterId(),
                entity.getNarrative(),
                entity.getDurationMinutes(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getApprovedBy(),
                entity.getApprovedAt());
    }
}
