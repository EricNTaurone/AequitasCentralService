package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.CustomerEntity;
import com.aequitas.aequitascentralservice.domain.model.Customer;
import lombok.experimental.UtilityClass;

/**
 * Mapper bridging {@link Customer} and {@link CustomerEntity}.
 */
@UtilityClass
public final class CustomerMapper {

    /**
     * Converts to domain aggregate.
     *
     * @param entity entity.
     * @return domain model.
     */
    public static Customer toDomain(final CustomerEntity entity) {
        return Customer.builder()
            .id(entity.getId())
            .firmId(entity.getFirmId())
            .name(entity.getName())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
