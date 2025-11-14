package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.ProjectEntity;
import com.aequitas.aequitascentralservice.domain.model.Project;

/**
 * Mapper bridging {@link Project} aggregates and persistence entities.
 */
public final class ProjectMapper {

    private ProjectMapper() {}

    /**
     * @param entity entity snapshot.
     * @return domain aggregate.
     */
    public static Project toDomain(final ProjectEntity entity) {
        return new Project(
                entity.getId(),
                entity.getFirmId(),
                entity.getCustomerId(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
