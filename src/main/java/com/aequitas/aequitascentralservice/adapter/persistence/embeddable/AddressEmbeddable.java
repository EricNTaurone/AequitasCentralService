package com.aequitas.aequitascentralservice.adapter.persistence.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable component representing a physical address in JPA entities.
 * 
 * <p>This class provides a reusable address structure that can be embedded
 * into any entity requiring address information. All fields are mapped to
 * columns prefixed with "address_" to maintain consistency with the existing
 * database schema.
 * 
 * <p><strong>Database Mapping:</strong>
 * <ul>
 *   <li>street → address_street (VARCHAR 255)</li>
 *   <li>city → address_city (VARCHAR 100)</li>
 *   <li>state → address_state (VARCHAR 100)</li>
 *   <li>postalCode → address_postal_code (VARCHAR 20)</li>
 *   <li>country → address_country (VARCHAR 100)</li>
 * </ul>
 * 
 * @since 1.0
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressEmbeddable {

    @Column(name = "address_street", nullable = false, length = 255)
    private String street;

    @Column(name = "address_city", nullable = false, length = 100)
    private String city;

    @Column(name = "address_state", nullable = false, length = 100)
    private String state;

    @Column(name = "address_postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "address_country", nullable = false, length = 100)
    private String country;
}
