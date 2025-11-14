package com.aequitas.aequitascentralservice.app.port.outbound;

import com.aequitas.aequitascentralservice.domain.model.Customer;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence abstraction for the customer aggregate.
 */
public interface CustomerRepositoryPort {

    /**
     * Finds a customer belonging to the provided firm.
     *
     * @param id customer identifier.
     * @param firmId tenant identifier.
     * @return optional aggregate.
     */
    Optional<Customer> findById(UUID id, UUID firmId);
}
