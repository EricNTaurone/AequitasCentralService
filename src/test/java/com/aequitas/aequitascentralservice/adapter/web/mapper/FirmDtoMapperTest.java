package com.aequitas.aequitascentralservice.adapter.web.mapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.Address;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.CreateFirmRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.FirmResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateFirmRequest;
import com.aequitas.aequitascentralservice.domain.command.CreateFirmCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateFirmCommand;
import com.aequitas.aequitascentralservice.domain.model.Firm;

/**
 * Unit tests for {@link FirmDtoMapper} covering all mapping scenarios between
 * REST DTOs and domain commands/models.
 *
 * <p>These tests verify that the static utility methods correctly transform
 * web-layer request/response objects to/from domain objects.
 */
class FirmDtoMapperTest {

    // ==================== toCommand(CreateFirmRequest) Tests ====================

    @Test
    void GIVEN_createRequest_WHEN_toCommand_THEN_mapsAllFields() {
        // GIVEN
        final CreateFirmRequest request = new CreateFirmRequest();
        request.setName("New Law Firm");
        request.setAddress(createDtoAddress());

        // WHEN
        final CreateFirmCommand command = FirmDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.name()).isEqualTo("New Law Firm");
        assertThat(command.address()).isNotNull();
        assertThat(command.address().street()).isEqualTo("123 Main Street");
        assertThat(command.address().city()).isEqualTo("New York");
        assertThat(command.address().state()).isEqualTo("NY");
        assertThat(command.address().postalCode()).isEqualTo("10001");
        assertThat(command.address().country()).isEqualTo("USA");
    }

    // ==================== toCommand(UpdateFirmRequest) Tests ====================

    @Test
    void GIVEN_updateRequestWithName_WHEN_toCommand_THEN_mapsName() {
        // GIVEN
        final UpdateFirmRequest request = new UpdateFirmRequest();
        request.setName("Updated Firm Name");

        // WHEN
        final UpdateFirmCommand command = FirmDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.name()).isPresent().contains("Updated Firm Name");
        assertThat(command.address()).isEmpty();
    }

    @Test
    void GIVEN_updateRequestWithAddress_WHEN_toCommand_THEN_mapsAddress() {
        // GIVEN
        final UpdateFirmRequest request = new UpdateFirmRequest();
        request.setAddress(createDtoAddress());

        // WHEN
        final UpdateFirmCommand command = FirmDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.name()).isEmpty();
        assertThat(command.address()).isPresent();
        assertThat(command.address().get().street()).isEqualTo("123 Main Street");
    }

    @Test
    void GIVEN_updateRequestWithBothFields_WHEN_toCommand_THEN_mapsBothFields() {
        // GIVEN
        final UpdateFirmRequest request = new UpdateFirmRequest();
        request.setName("Updated Name");
        request.setAddress(createDtoAddress());

        // WHEN
        final UpdateFirmCommand command = FirmDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.name()).isPresent().contains("Updated Name");
        assertThat(command.address()).isPresent();
    }

    @Test
    void GIVEN_updateRequestWithNullFields_WHEN_toCommand_THEN_mapsToEmpty() {
        // GIVEN
        final UpdateFirmRequest request = new UpdateFirmRequest();
        request.setName(null);
        request.setAddress(null);

        // WHEN
        final UpdateFirmCommand command = FirmDtoMapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.name()).isEmpty();
        assertThat(command.address()).isEmpty();
    }

    // ==================== toResponse(Firm) Tests ====================

    @Test
    void GIVEN_domainFirm_WHEN_toResponse_THEN_mapsAllFields() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final Instant createdAt = Instant.ofEpochMilli(1700000000000L);
        final Instant updatedAt = Instant.ofEpochMilli(1700050000000L);
        final Firm firm = Firm.builder()
                .id(id)
                .name("Smith & Associates")
                .address(createDomainAddress())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // WHEN
        final FirmResponse response = FirmDtoMapper.toResponse(firm);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Smith & Associates");
        assertThat(response.getAddress()).isNotNull();
        assertThat(response.getAddress().getStreet()).isEqualTo("123 Main Street");
        assertThat(response.getAddress().getCity()).isEqualTo("New York");
        assertThat(response.getAddress().getState()).isEqualTo("NY");
        assertThat(response.getAddress().getPostalCode()).isEqualTo("10001");
        assertThat(response.getAddress().getCountry()).isEqualTo("USA");
        assertThat(response.getCreatedTime()).isEqualTo(1700000000000L);
        assertThat(response.getLastUpdateTime()).isEqualTo(1700050000000L);
    }

    @Test
    void GIVEN_firmWithDifferentAddress_WHEN_toResponse_THEN_mapsAddressCorrectly() {
        // GIVEN
        final com.aequitas.aequitascentralservice.domain.value.Address address =
                com.aequitas.aequitascentralservice.domain.value.Address.builder()
                        .street("456 Broadway")
                        .city("Boston")
                        .state("MA")
                        .postalCode("02101")
                        .country("USA")
                        .build();
        final Firm firm = Firm.builder()
                .id(UUID.randomUUID())
                .name("Test Firm")
                .address(address)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // WHEN
        final FirmResponse response = FirmDtoMapper.toResponse(firm);

        // THEN
        assertThat(response.getAddress().getStreet()).isEqualTo("456 Broadway");
        assertThat(response.getAddress().getCity()).isEqualTo("Boston");
        assertThat(response.getAddress().getState()).isEqualTo("MA");
        assertThat(response.getAddress().getPostalCode()).isEqualTo("02101");
        assertThat(response.getAddress().getCountry()).isEqualTo("USA");
    }

    @Test
    void GIVEN_firmWithTimestamps_WHEN_toResponse_THEN_convertsToEpochMillis() {
        // GIVEN
        final Instant createdAt = Instant.ofEpochMilli(1600000000000L);
        final Instant updatedAt = Instant.ofEpochMilli(1600000060000L);
        final Firm firm = Firm.builder()
                .id(UUID.randomUUID())
                .name("Test Firm")
                .address(createDomainAddress())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // WHEN
        final FirmResponse response = FirmDtoMapper.toResponse(firm);

        // THEN
        assertThat(response.getCreatedTime()).isEqualTo(1600000000000L);
        assertThat(response.getLastUpdateTime()).isEqualTo(1600000060000L);
    }

    // ==================== Helper Methods ====================

    private Address createDtoAddress() {
        final Address address = new Address();
        address.setStreet("123 Main Street");
        address.setCity("New York");
        address.setState("NY");
        address.setPostalCode("10001");
        address.setCountry("USA");
        return address;
    }

    private com.aequitas.aequitascentralservice.domain.value.Address createDomainAddress() {
        return com.aequitas.aequitascentralservice.domain.value.Address.builder()
                .street("123 Main Street")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();
    }
}
