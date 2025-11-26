package com.aequitas.aequitascentralservice.domain.command;

import com.aequitas.aequitascentralservice.domain.value.Address;

import lombok.Builder;

/**
 * Command to create a new firm.
 *
 * @param name firm's legal or operating name.
 * @param address firm's physical address.
 */
@Builder
public record CreateFirmCommand(
        String name,
        Address address) {
}
