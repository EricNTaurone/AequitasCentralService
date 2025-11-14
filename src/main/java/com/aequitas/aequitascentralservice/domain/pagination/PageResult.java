package com.aequitas.aequitascentralservice.domain.pagination;

import java.util.List;

/**
 * Represents a stable page of results with a cursor for the next batch.
 *
 * @param items    payload items inside the page.
 * @param nextCursor cursor pointing to the next page or {@code null} if exhausted.
 * @param totalItems number of items retrieved.
 * @param hasMore whether more data is available.
 * @param <T>     payload type.
 */
public record PageResult<T>(List<T> items, String nextCursor, long totalItems, boolean hasMore) {
}
