package com.aequitas.aequitascentralservice.adapter.web.exception;

import org.springframework.http.ProblemDetail;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class ErrorMessage implements IErrorMessage {
    @Nonnull
    private String message;
    @Nullable
    private StackTraceElement[] stackTrace;
    @Nullable
    private ThrowableInfo throwable;
    @Nullable
    private String error;
    @Nullable
    private ProblemDetail problemDetail;
}
