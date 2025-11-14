package com.aequitas.aequitascentralservice.adapter.persistence;

import com.aequitas.aequitascentralservice.adapter.persistence.mapper.ProjectMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.ProjectJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.ProjectRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.Project;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * JPA adapter for {@link ProjectRepositoryPort}.
 */
@Component
public class ProjectRepositoryAdapter implements ProjectRepositoryPort {

    private final ProjectJpaRepository repository;

    public ProjectRepositoryAdapter(final ProjectJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Project> findById(final UUID id, final UUID firmId) {
        return repository.findByIdAndFirmId(id, firmId).map(ProjectMapper::toDomain);
    }
}
