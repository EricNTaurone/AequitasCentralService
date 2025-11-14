package com.aequitas.aequitascentralservice.domain.pagination;

/**
 * Describes cursor-based pagination parameters.
 *
 * @param limit  maximum records to fetch.
 * @param cursor opaque cursor representing the last seen record id.
 */
public record PageRequest(int limit, String cursor) {
}
