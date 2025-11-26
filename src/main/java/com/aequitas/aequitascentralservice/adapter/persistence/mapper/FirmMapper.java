package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import com.aequitas.aequitascentralservice.adapter.persistence.embeddable.AddressEmbeddable;
import com.aequitas.aequitascentralservice.adapter.persistence.entity.FirmEntity;
import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.value.Address;

import lombok.experimental.UtilityClass;

/**
 * Mapper bridging {@link Firm} aggregates and {@link FirmEntity} rows.
 * 
 * <p>This utility class provides bidirectional mapping between the domain model's
 * {@link Firm} aggregate and the persistence layer's {@link FirmEntity}. Address
 * components are embedded using JPA's {@link jakarta.persistence.Embeddable} pattern.
 * 
 * <p><strong>Mapping Strategy:</strong>
 * <ul>
 *   <li>{@link Address} value object is converted to/from {@link AddressEmbeddable}
 *       which handles the relational column mapping</li>
 *   <li>Timestamps are preserved as {@link Instant} for temporal accuracy</li>
 *   <li>All mappings are bidirectional and lossless</li>
 * </ul>
 * 
 * <p><strong>Thread-Safety:</strong> All methods are static and stateless, making
 * this class inherently thread-safe.
 * 
 * @see Firm
 * @see FirmEntity
 * @see Address
 * @since 1.0
 */
@UtilityClass
public final class FirmMapper {

    /**
     * Converts a domain aggregate into a managed entity.
     *
     * @param firm aggregate.
     * @return entity snapshot.
     */
    public static FirmEntity toEntity(final Firm firm) {
        final AddressEmbeddable addressEmbeddable = AddressEmbeddable.builder()
                .street(firm.getAddress().street())
                .city(firm.getAddress().city())
                .state(firm.getAddress().state())
                .postalCode(firm.getAddress().postalCode())
                .country(firm.getAddress().country())
                .build();

        return FirmEntity.builder()
                .id(firm.getId())
                .name(firm.getName())
                .address(addressEmbeddable)
                .createdAt(firm.getCreatedAt())
                .updatedAt(firm.getUpdatedAt())
                .build();
    }

    /**
     * Converts a persistence entity into a domain aggregate.
     *
     * @param entity managed entity.
     * @return aggregate snapshot.
     */
    public static Firm toDomain(final FirmEntity entity) {
        final Address address = Address.builder()
                .street(entity.getAddress().getStreet())
                .city(entity.getAddress().getCity())
                .state(entity.getAddress().getState())
                .postalCode(entity.getAddress().getPostalCode())
                .country(entity.getAddress().getCountry())
                .build();

        return Firm.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(address)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
