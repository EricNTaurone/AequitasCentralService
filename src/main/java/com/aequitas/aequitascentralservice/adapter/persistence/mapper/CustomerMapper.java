package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.CustomerEntity;
import com.aequitas.aequitascentralservice.domain.model.Customer;

/**
 * Mapper bridging {@link Customer} and {@link CustomerEntity}.
 */
public final class CustomerMapper {

    private CustomerMapper() {}

    /**
     * Converts to domain aggregate.
     *
     * @param entity entity.
     * @return domain model.
     */
    public static Customer toDomain(final CustomerEntity entity) {
        return new Customer(entity.getId(), entity.getFirmId(), entity.getName(), entity.getCreatedAt());
    }
}
