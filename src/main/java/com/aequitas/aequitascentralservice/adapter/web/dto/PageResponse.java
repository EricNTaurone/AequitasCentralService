package com.aequitas.aequitascentralservice.adapter.web.dto;

import java.util.List;

/**
 * Generic DTO representing a paginated API response.
 *
 * @param items payload items.
 * @param nextCursor cursor for subsequent pages.
 * @param total total returned items.
 * @param hasMore whether supply-side data remains.
 * @param <T> payload type.
 */
public record PageResponse<T>(List<T> items, String nextCursor, long total, boolean hasMore) {
}
