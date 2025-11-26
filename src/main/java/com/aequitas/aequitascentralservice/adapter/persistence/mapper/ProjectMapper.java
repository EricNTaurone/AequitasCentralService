package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.ProjectEntity;
import com.aequitas.aequitascentralservice.domain.model.Project;

import lombok.experimental.UtilityClass;

/**
 * Mapper bridging {@link Project} aggregates and persistence entities.
 */
@UtilityClass
public final class ProjectMapper {

    /**
     * @param entity entity snapshot.
     * @return domain aggregate.
     */
    public static Project toDomain(final ProjectEntity entity) {
        return Project.builder()
                .id(entity.getId())
                .firmId(entity.getFirmId())
                .customerId(entity.getCustomerId())
                .name(entity.getName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
