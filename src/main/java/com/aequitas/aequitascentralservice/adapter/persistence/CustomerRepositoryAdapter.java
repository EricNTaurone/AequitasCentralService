package com.aequitas.aequitascentralservice.adapter.persistence;

import com.aequitas.aequitascentralservice.adapter.persistence.mapper.CustomerMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.CustomerJpaRepository;
import com.aequitas.aequitascentralservice.app.port.outbound.CustomerRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * JPA adapter for {@link CustomerRepositoryPort}.
 */
@Component
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository repository;

    public CustomerRepositoryAdapter(final CustomerJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Customer> findById(final UUID id, final UUID firmId) {
        return repository.findByIdAndFirmId(id, firmId).map(CustomerMapper::toDomain);
    }
}
