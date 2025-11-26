package com.aequitas.aequitascentralservice.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.aequitas.aequitascentralservice.domain.value.Address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Aggregate root representing a firm (tenant) within the multi-tenant architecture.
 * 
 * <p>A Firm encapsulates the organizational entity that owns users, time entries, customers,
 * and projects. All data is isolated at the firm level, enforcing tenant boundaries through
 * row-level security and application-layer filtering.
 * 
 * <p><strong>Aggregate Root Responsibilities:</strong>
 * <ul>
 *   <li>Enforces invariants related to firm identity and address validity</li>
 *   <li>Provides factory methods for creating and updating firm instances</li>
 *   <li>Maintains audit timestamps (created/updated) for compliance tracking</li>
 * </ul>
 * 
 * <p><strong>Immutability:</strong> All fields are final; updates produce new instances
 * via the {@link #update(String, Address, Instant)} method, following functional
 * programming principles.
 * 
 * <p><strong>Thread-Safety:</strong> This class is immutable and thread-safe.
 * 
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * // Create a new firm
 * Address address = Address.builder()
 *     .street("123 Main St")
 *     .city("New York")
 *     .state("NY")
 *     .postalCode("10001")
 *     .country("USA")
 *     .build();
 * Firm firm = Firm.create("Smith & Associates", address, Instant.now());
 * 
 * // Update firm details
 * Firm updated = firm.update("Smith & Partners LLP", null, Instant.now());
 * }</pre>
 * 
 * @see Address
 * @since 1.0
 */
@Builder
@AllArgsConstructor
@Value
public final class Firm {

    private final UUID id;
    private final String name;
    private final Address address;
    private final Instant createdAt;
    private final Instant updatedAt;

    /**
     * Rehydrates a firm aggregate from persistence.
     *
     * @param id unique firm identifier.
     * @param name firm's legal or operating name.
     * @param address firm's physical address.
     * @param createdAt creation timestamp.
     * @param updatedAt last update timestamp.
     * @return aggregate snapshot.
     */
    public static Firm rehydrate(
            final UUID id,
            final String name,
            final Address address,
            final Instant createdAt,
            final Instant updatedAt) {
        return Firm.builder()
                .id(id)
                .name(name)
                .address(address)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Factory that creates a new firm with the provided details.
     *
     * @param name firm's legal or operating name.
     * @param address firm's physical address.
     * @param now clock instant used for audit fields.
     * @return immutable firm aggregate.
     */
    public static Firm create(
            final String name,
            final Address address,
            final Instant now) {
        return Firm.builder()
                .id(UUID.randomUUID())
                .name(name)
                .address(address)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Creates an updated copy of this firm with new attributes.
     *
     * @param newName updated firm name (or existing if null).
     * @param newAddress updated address (or existing if null).
     * @param now clock instant for the update timestamp.
     * @return new immutable firm aggregate with updated values.
     */
    public Firm update(
            final String newName,
            final Address newAddress,
            final Instant now) {
        return Firm.builder()
                .id(this.id)
                .name(newName != null ? newName : this.name)
                .address(newAddress != null ? newAddress : this.address)
                .createdAt(this.createdAt)
                .updatedAt(now)
                .build();
    }
}
