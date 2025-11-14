package com.aequitas.aequitascentralservice.adapter.web.dto;

import java.util.UUID;

/**
 * Wrapper for responses that only need to return an identifier.
 *
 * @param id generated identifier.
 */
public record IdResponse(UUID id) {
}
