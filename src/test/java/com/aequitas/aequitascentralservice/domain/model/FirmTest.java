package com.aequitas.aequitascentralservice.domain.model;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.domain.value.Address;

/**
 * Unit tests covering invariants on the {@link Firm} aggregate.
 * Tests follow GIVEN_WHEN_THEN naming convention with comprehensive coverage.
 */
class FirmTest {

    // ====================== create() Factory Tests ======================

    @Test
    void GIVEN_validParameters_WHEN_create_THEN_createsFirmWithGeneratedId() {
        // GIVEN
        final String name = "Smith & Associates";
        final Address address = createAddress();
        final Instant now = Instant.now();

        // WHEN
        final Firm firm = Firm.create(name, address, now);

        // THEN
        assertThat(firm.getId()).isNotNull();
        assertThat(firm.getName()).isEqualTo(name);
        assertThat(firm.getAddress()).isEqualTo(address);
        assertThat(firm.getCreatedAt()).isEqualTo(now);
        assertThat(firm.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void GIVEN_multipleCreations_WHEN_create_THEN_generatesUniqueIds() {
        // GIVEN
        final String name = "Test Firm";
        final Address address = createAddress();
        final Instant now = Instant.now();

        // WHEN
        final Firm firm1 = Firm.create(name, address, now);
        final Firm firm2 = Firm.create(name, address, now);

        // THEN
        assertThat(firm1.getId()).isNotEqualTo(firm2.getId());
    }

    // ====================== rehydrate() Factory Tests ======================

    @Test
    void GIVEN_validParameters_WHEN_rehydrate_THEN_restoresFirmAggregate() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final String name = "Johnson Law Firm";
        final Address address = createAddress();
        final Instant createdAt = Instant.now().minusSeconds(3600);
        final Instant updatedAt = Instant.now();

        // WHEN
        final Firm firm = Firm.rehydrate(id, name, address, createdAt, updatedAt);

        // THEN
        assertThat(firm.getId()).isEqualTo(id);
        assertThat(firm.getName()).isEqualTo(name);
        assertThat(firm.getAddress()).isEqualTo(address);
        assertThat(firm.getCreatedAt()).isEqualTo(createdAt);
        assertThat(firm.getUpdatedAt()).isEqualTo(updatedAt);
    }

    // ====================== update() Method Tests ======================

    @Test
    void GIVEN_newName_WHEN_update_THEN_updatesNameAndTimestamp() {
        // GIVEN
        final Firm original = Firm.create("Original Name", createAddress(), Instant.now());
        final String newName = "Updated Name";
        final Instant updateTime = Instant.now().plusSeconds(60);

        // WHEN
        final Firm updated = original.update(newName, null, updateTime);

        // THEN
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getName()).isEqualTo(newName);
        assertThat(updated.getAddress()).isEqualTo(original.getAddress());
        assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isEqualTo(updateTime);
    }

    @Test
    void GIVEN_newAddress_WHEN_update_THEN_updatesAddressAndTimestamp() {
        // GIVEN
        final Firm original = Firm.create("Test Firm", createAddress(), Instant.now());
        final Address newAddress = Address.builder()
                .street("456 New Street")
                .city("Boston")
                .state("MA")
                .postalCode("02101")
                .country("USA")
                .build();
        final Instant updateTime = Instant.now().plusSeconds(60);

        // WHEN
        final Firm updated = original.update(null, newAddress, updateTime);

        // THEN
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getName()).isEqualTo(original.getName());
        assertThat(updated.getAddress()).isEqualTo(newAddress);
        assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isEqualTo(updateTime);
    }

    @Test
    void GIVEN_bothNewNameAndAddress_WHEN_update_THEN_updatesBothFields() {
        // GIVEN
        final Firm original = Firm.create("Original Name", createAddress(), Instant.now());
        final String newName = "New Name";
        final Address newAddress = Address.builder()
                .street("789 Another St")
                .city("Chicago")
                .state("IL")
                .postalCode("60601")
                .country("USA")
                .build();
        final Instant updateTime = Instant.now().plusSeconds(60);

        // WHEN
        final Firm updated = original.update(newName, newAddress, updateTime);

        // THEN
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getName()).isEqualTo(newName);
        assertThat(updated.getAddress()).isEqualTo(newAddress);
        assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isEqualTo(updateTime);
    }

    @Test
    void GIVEN_nullUpdates_WHEN_update_THEN_preservesOriginalValues() {
        // GIVEN
        final Firm original = Firm.create("Test Firm", createAddress(), Instant.now());
        final Instant updateTime = Instant.now().plusSeconds(60);

        // WHEN
        final Firm updated = original.update(null, null, updateTime);

        // THEN
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getName()).isEqualTo(original.getName());
        assertThat(updated.getAddress()).isEqualTo(original.getAddress());
        assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isEqualTo(updateTime);
    }

    // ====================== Immutability Tests ======================

    @Test
    void GIVEN_firmCreated_WHEN_update_THEN_originalRemainsUnchanged() {
        // GIVEN
        final Firm original = Firm.create("Original Name", createAddress(), Instant.now());
        final String originalName = original.getName();
        final Address originalAddress = original.getAddress();

        // WHEN
        final Firm updated = original.update("New Name", null, Instant.now().plusSeconds(60));

        // THEN
        assertThat(original.getName()).isEqualTo(originalName);
        assertThat(original.getAddress()).isEqualTo(originalAddress);
        assertThat(updated.getName()).isNotEqualTo(original.getName());
    }

    // ====================== Helper Methods ======================

    private Address createAddress() {
        return Address.builder()
                .street("123 Main Street")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();
    }
}
