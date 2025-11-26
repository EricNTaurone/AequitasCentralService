package com.aequitas.aequitascentralservice.domain.command;

import java.util.Optional;

import com.aequitas.aequitascentralservice.domain.value.Address;

import lombok.Builder;

/**
 * Command to update an existing firm's details.
 *
 * @param name optional new firm name.
 * @param address optional new firm address.
 */
@Builder
public record UpdateFirmCommand(
        Optional<String> name,
        Optional<Address> address) {
}
