package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.TimeEntryEntity;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;

import lombok.experimental.UtilityClass;

/**
 * Mapper bridging {@link TimeEntry} aggregates and {@link TimeEntryEntity}
 * rows.
 */
@UtilityClass
public final class TimeEntryMapper {

    /**
     * Converts a domain aggregate into a managed entity.
     *
     * @param entry aggregate.
     * @return entity snapshot.
     */
    public static TimeEntryEntity toEntity(final TimeEntry entry) {
        return TimeEntryEntity.builder()
                .id(entry.getId())
                .firmId(entry.getFirmId())
                .userId(entry.getUserId())
                .customerId(entry.getCustomerId())
                .projectId(entry.getProjectId())
                .matterId(entry.getMatterId())
                .narrative(entry.getNarrative())
                .durationMinutes(entry.getDurationMinutes())
                .status(entry.getStatus())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .approvedBy(entry.getApprovedBy())
                .approvedAt(entry.getApprovedAt())
                .build();
    }

    /**
     * Converts a persistence entity into a domain aggregate.
     *
     * @param entity managed entity.
     * @return aggregate snapshot.
     */
    public static TimeEntry toDomain(final TimeEntryEntity entity) {
        return TimeEntry.builder()
                .id(entity.getId())
                .firmId(entity.getFirmId())
                .userId(entity.getUserId())
                .customerId(entity.getCustomerId())
                .projectId(entity.getProjectId())
                .matterId(entity.getMatterId())
                .narrative(entity.getNarrative())
                .durationMinutes(entity.getDurationMinutes())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .build();
    }
}
