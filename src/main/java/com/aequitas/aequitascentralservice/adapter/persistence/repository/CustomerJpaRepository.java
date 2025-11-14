package com.aequitas.aequitascentralservice.adapter.persistence.repository;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.CustomerEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link CustomerEntity}.
 */
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

    /**
     * @param id customer identifier.
     * @param firmId tenant identifier.
     * @return optional entity limited to the supplied firm.
     */
    Optional<CustomerEntity> findByIdAndFirmId(UUID id, UUID firmId);
}
