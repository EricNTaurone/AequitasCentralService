package com.aequitas.aequitascentralservice.domain.value;

import lombok.Builder;

/**
 * Represents a physical address for a firm or other entity.
 * 
 * <p>This value object encapsulates all components of a postal address following
 * standard international address formats. It is immutable and thread-safe.
 * 
 * <p><strong>Immutability:</strong> All fields are final; modifications require
 * creating a new instance via the builder pattern.
 * 
 * <p><strong>Validation:</strong> This record does not enforce validation constraints;
 * validation should be applied at the application or adapter layer.
 * 
 * @param street street address line (e.g., "123 Main Street").
 * @param city city name (e.g., "New York").
 * @param state state, province, or region (e.g., "NY", "California").
 * @param postalCode postal or ZIP code (e.g., "10001", "SW1A 1AA").
 * @param country country name (e.g., "USA", "United Kingdom").
 * 
 * @since 1.0
 */
@Builder
public record Address(
        String street,
        String city,
        String state,
        String postalCode,
        String country) {
}
