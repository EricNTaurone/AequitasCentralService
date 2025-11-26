package com.aequitas.aequitascentralservice.adapter.web.mapper;

import java.time.Instant;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.Address;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.CreateFirmRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.FirmResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateFirmRequest;
import com.aequitas.aequitascentralservice.domain.command.CreateFirmCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateFirmCommand;
import com.aequitas.aequitascentralservice.domain.model.Firm;

import lombok.experimental.UtilityClass;

/**
 * Maps between REST DTOs and domain models/commands for firm operations.
 * 
 * <p>This utility class bridges the web layer's OpenAPI-generated DTOs and the
 * application layer's domain commands/models, ensuring clean separation between
 * API contracts and domain logic.
 * 
 * <p><strong>Mapping Responsibilities:</strong>
 * <ul>
 *   <li>Converts REST request DTOs ({@code CreateFirmRequest}, {@code UpdateFirmRequest})
 *       into domain commands</li>
 *   <li>Converts domain aggregates ({@link Firm}) into REST response DTOs
 *       ({@code FirmResponse})</li>
 *   <li>Transforms timestamp representations between {@link Instant} and epoch
 *       milliseconds</li>
 *   <li>Handles optional fields in update requests via {@link java.util.Optional}</li>
 * </ul>
 * 
 * <p><strong>Thread-Safety:</strong> All methods are static and stateless, making
 * this class inherently thread-safe.
 * 
 * @see Firm
 * @see Address
 * @since 1.0
 */
@UtilityClass
public final class FirmDtoMapper {

    /**
     * Converts a REST create request into a domain command.
     *
     * @param request REST DTO from client.
     * @return domain command.
     */
    public static CreateFirmCommand toCommand(final CreateFirmRequest request) {
        return CreateFirmCommand.builder()
                .name(request.getName())
                .address(toDomainAddress(request.getAddress()))
                .build();
    }

    /**
     * Converts a REST update request into a domain command.
     *
     * @param request REST DTO from client.
     * @return domain command.
     */
    public static UpdateFirmCommand toCommand(final UpdateFirmRequest request) {
        return UpdateFirmCommand.builder()
                .name(java.util.Optional.ofNullable(request.getName()))
                .address(java.util.Optional.ofNullable(request.getAddress()).map(FirmDtoMapper::toDomainAddress))
                .build();
    }

    /**
     * Converts a domain firm aggregate into a REST response DTO.
     *
     * @param firm domain aggregate.
     * @return REST DTO for client.
     */
    public static FirmResponse toResponse(final Firm firm) {
        final FirmResponse response = new FirmResponse();
        response.setId(firm.getId());
        response.setName(firm.getName());
        response.setAddress(toResponseAddress(firm.getAddress()));
        response.setCreatedTime(firm.getCreatedAt().toEpochMilli());
        response.setLastUpdateTime(firm.getUpdatedAt().toEpochMilli());
        return response;
    }

    /**
     * Converts a REST address DTO to a domain address value object.
     *
     * @param address REST address DTO.
     * @return domain address value object.
     */
    private static com.aequitas.aequitascentralservice.domain.value.Address toDomainAddress(
            final Address address) {
        return com.aequitas.aequitascentralservice.domain.value.Address.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }

    /**
     * Converts a domain address value object to a REST address DTO.
     *
     * @param address domain address value object.
     * @return REST address DTO.
     */
    private static Address toResponseAddress(
            final com.aequitas.aequitascentralservice.domain.value.Address address) {
        final Address dto = new Address();
        dto.setStreet(address.street());
        dto.setCity(address.city());
        dto.setState(address.state());
        dto.setPostalCode(address.postalCode());
        dto.setCountry(address.country());
        return dto;
    }
}
