package com.aequitas.aequitascentralservice.adapter.web.exception;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * A JSON-serializable representation of a Throwable.
 * <p>
 * This class captures the essential information from a Throwable without the
 * circular reference issues that prevent Jackson from serializing Throwable directly.
 */
@Builder
@Data
@Jacksonized
public class ThrowableInfo {
    @Nullable
    private String type;
    @Nullable
    private String message;
    @Nullable
    private StackTraceElement[] stackTrace;
    @Nullable
    private ThrowableInfo cause;

    /**
     * Creates a ThrowableInfo from a Throwable, recursively capturing the cause chain.
     *
     * @param throwable the throwable to convert.
     * @return ThrowableInfo representation, or null if throwable is null.
     */
    public static ThrowableInfo from(final Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return ThrowableInfo.builder()
            .type(throwable.getClass().getName())
            .message(throwable.getMessage())
            .stackTrace(throwable.getStackTrace())
            .cause(from(throwable.getCause()))
            .build();
    }
}
