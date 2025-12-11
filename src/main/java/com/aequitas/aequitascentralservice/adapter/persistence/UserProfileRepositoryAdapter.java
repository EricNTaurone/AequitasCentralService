package com.aequitas.aequitascentralservice.adapter.persistence;

import com.aequitas.aequitascentralservice.adapter.persistence.mapper.UserProfileMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.UserProfileJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.UserProfileRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adapter bridging user profile persistence and the domain.
 */
@Component
public class UserProfileRepositoryAdapter implements UserProfileRepositoryPort {

    private final UserProfileJpaRepository repository;

    public UserProfileRepositoryAdapter(final UserProfileJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserProfile> findById(final UUID id, final UUID firmId) {
        return repository.findByIdAndFirmId(id, firmId).map(UserProfileMapper::toDomain);
    }

    @Override
    public Optional<UserProfile> findByAuthenticationId(final UUID authenticationId) {
        return repository.findByAuthenticationId(authenticationId).map(UserProfileMapper::toDomain);
    }

    @Override
    public List<UserProfile> findByFirmId(final UUID firmId) {
        return repository.findByFirmId(firmId).stream().map(UserProfileMapper::toDomain).toList();
    }

    @Override
    public List<UserProfile> findByFirmIdAndRole(final UUID firmId, final Role role) {
        return repository.findByFirmIdAndRole(firmId, role).stream()
                .map(UserProfileMapper::toDomain)
                .toList();
    }

    @Override
    public UserProfile save(final UserProfile profile) {
        return UserProfileMapper.toDomain(repository.save(UserProfileMapper.toEntity(profile)));
    }
}
