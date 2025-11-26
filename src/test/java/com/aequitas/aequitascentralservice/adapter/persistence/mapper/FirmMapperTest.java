package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.persistence.embeddable.AddressEmbeddable;
import com.aequitas.aequitascentralservice.adapter.persistence.entity.FirmEntity;
import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.value.Address;

/**
 * Unit tests for {@link FirmMapper} covering bidirectional mapping between
 * domain aggregates and persistence entities.
 */
class FirmMapperTest {

    // ==================== toEntity() Tests ====================

    @Test
    void GIVEN_domainFirm_WHEN_toEntity_THEN_mapsAllFields() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final Address address = Address.builder()
                .street("123 Main Street")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();
        final Instant now = Instant.now();
        final Firm firm = Firm.builder()
                .id(id)
                .name("Test Firm")
                .address(address)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // WHEN
        final FirmEntity entity = FirmMapper.toEntity(firm);

        // THEN
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Test Firm");
        assertThat(entity.getAddress()).isNotNull();
        assertThat(entity.getAddress().getStreet()).isEqualTo("123 Main Street");
        assertThat(entity.getAddress().getCity()).isEqualTo("New York");
        assertThat(entity.getAddress().getState()).isEqualTo("NY");
        assertThat(entity.getAddress().getPostalCode()).isEqualTo("10001");
        assertThat(entity.getAddress().getCountry()).isEqualTo("USA");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void GIVEN_firmWithDifferentAddress_WHEN_toEntity_THEN_mapsAddressCorrectly() {
        // GIVEN
        final Address address = Address.builder()
                .street("456 Broadway")
                .city("Boston")
                .state("MA")
                .postalCode("02101")
                .country("USA")
                .build();
        final Firm firm = Firm.builder()
                .id(UUID.randomUUID())
                .name("Another Firm")
                .address(address)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final FirmEntity entity = FirmMapper.toEntity(firm);

        // THEN
        assertThat(entity.getAddress()).isNotNull();
        assertThat(entity.getAddress().getStreet()).isEqualTo("456 Broadway");
        assertThat(entity.getAddress().getCity()).isEqualTo("Boston");
        assertThat(entity.getAddress().getState()).isEqualTo("MA");
        assertThat(entity.getAddress().getPostalCode()).isEqualTo("02101");
        assertThat(entity.getAddress().getCountry()).isEqualTo("USA");
    }

    // ==================== toDomain() Tests ====================

    @Test
    void GIVEN_firmEntity_WHEN_toDomain_THEN_mapsAllFields() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final Instant now = Instant.now();
        final AddressEmbeddable addressEmbeddable = AddressEmbeddable.builder()
                .street("123 Main Street")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();
        final FirmEntity entity = FirmEntity.builder()
                .id(id)
                .name("Test Firm")
                .address(addressEmbeddable)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // WHEN
        final Firm firm = FirmMapper.toDomain(entity);

        // THEN
        assertThat(firm).isNotNull();
        assertThat(firm.getId()).isEqualTo(id);
        assertThat(firm.getName()).isEqualTo("Test Firm");
        assertThat(firm.getAddress()).isNotNull();
        assertThat(firm.getAddress().street()).isEqualTo("123 Main Street");
        assertThat(firm.getAddress().city()).isEqualTo("New York");
        assertThat(firm.getAddress().state()).isEqualTo("NY");
        assertThat(firm.getAddress().postalCode()).isEqualTo("10001");
        assertThat(firm.getAddress().country()).isEqualTo("USA");
        assertThat(firm.getCreatedAt()).isEqualTo(now);
        assertThat(firm.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void GIVEN_entityWithDifferentAddress_WHEN_toDomain_THEN_reconstructsAddressValueObject() {
        // GIVEN
        final AddressEmbeddable addressEmbeddable = AddressEmbeddable.builder()
                .street("789 Market St")
                .city("San Francisco")
                .state("CA")
                .postalCode("94102")
                .country("USA")
                .build();
        final FirmEntity entity = FirmEntity.builder()
                .id(UUID.randomUUID())
                .name("Another Firm")
                .address(addressEmbeddable)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final Firm firm = FirmMapper.toDomain(entity);

        // THEN
        assertThat(firm.getAddress()).isNotNull();
        assertThat(firm.getAddress().street()).isEqualTo("789 Market St");
        assertThat(firm.getAddress().city()).isEqualTo("San Francisco");
        assertThat(firm.getAddress().state()).isEqualTo("CA");
        assertThat(firm.getAddress().postalCode()).isEqualTo("94102");
        assertThat(firm.getAddress().country()).isEqualTo("USA");
    }

    // ==================== Bidirectional Mapping Tests ====================

    @Test
    void GIVEN_domainFirm_WHEN_toEntityAndBackToDomain_THEN_preservesAllFields() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final Address address = Address.builder()
                .street("123 Test St")
                .city("Test City")
                .state("TC")
                .postalCode("12345")
                .country("Test Country")
                .build();
        final Instant createdAt = Instant.now().minusSeconds(3600);
        final Instant updatedAt = Instant.now();
        final Firm original = Firm.builder()
                .id(id)
                .name("Original Firm")
                .address(address)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // WHEN
        final FirmEntity entity = FirmMapper.toEntity(original);
        final Firm reconstructed = FirmMapper.toDomain(entity);

        // THEN
        assertThat(reconstructed.getId()).isEqualTo(original.getId());
        assertThat(reconstructed.getName()).isEqualTo(original.getName());
        assertThat(reconstructed.getAddress().street()).isEqualTo(original.getAddress().street());
        assertThat(reconstructed.getAddress().city()).isEqualTo(original.getAddress().city());
        assertThat(reconstructed.getAddress().state()).isEqualTo(original.getAddress().state());
        assertThat(reconstructed.getAddress().postalCode()).isEqualTo(original.getAddress().postalCode());
        assertThat(reconstructed.getAddress().country()).isEqualTo(original.getAddress().country());
        assertThat(reconstructed.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(reconstructed.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }
}
