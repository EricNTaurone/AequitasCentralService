package com.aequitas.aequitascentralservice.app.port.outbound;

import com.aequitas.aequitascentralservice.domain.model.Project;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence abstraction for project aggregates.
 */
public interface ProjectRepositoryPort {

    /**
     * Finds a project within the same tenant.
     *
     * @param id project identifier.
     * @param firmId tenant identifier.
     * @return optional aggregate.
     */
    Optional<Project> findById(UUID id, UUID firmId);
}
