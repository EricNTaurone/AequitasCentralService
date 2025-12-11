package com.aequitas.aequitascentralservice.adapter.web.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aequitas.aequitascentralservice.config.Environment;

/**
 * Translates exceptions into {@link ErrorMessage} responses containing RFC 7807 {@link ProblemDetail}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active}")
    private String environment;

    /**
     * Handles validation failures triggered by bean validation.
     *
     * @param ex thrown exception.
     * @return error message with problem detail payload.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidation(final MethodArgumentNotValidException ex) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problemDetail.setTitle("VALIDATION");
        final Map<String, String> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("details", details);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
            .body(createErrorMessage(ex, "Validation failed", problemDetail));
    }

    /**
     * Handles illegal arguments which usually indicate a 400 response.
     *
     * @param ex thrown exception.
     * @return error message with problem detail.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgument(final IllegalArgumentException ex) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("VALIDATION");
        problemDetail.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorMessage(ex, "Invalid argument", problemDetail));
    }

    /**
     * Handles illegal state exceptions raised during business logic evaluation.
     *
     * @param ex thrown exception.
     * @return error message with problem detail.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorMessage> handleIllegalState(final IllegalStateException ex) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("CONFLICT");
        problemDetail.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(createErrorMessage(ex, "Conflict state", problemDetail));
    }

    /**
     * Handles authorization failures triggered by business rules.
     *
     * @param ex thrown exception.
     * @return error message with problem detail with 403 status.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAccessDenied(final AccessDeniedException ex) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setTitle("FORBIDDEN");
        problemDetail.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(createErrorMessage(ex, "Access denied", problemDetail));
    }

    /**
     * Handles JSON parsing/deserialization errors when request body cannot be read.
     * This includes invalid enum values, malformed JSON, type mismatches, etc.
     *
     * @param ex thrown exception.
     * @return error message with problem detail with 400 status.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("VALIDATION");

        // Extract the most useful error message from the exception chain
        Throwable cause = ex.getCause();
        if (cause != null) {
            problemDetail.setDetail(cause.getMessage());
        } else {
            problemDetail.setDetail(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorMessage(ex, "Message not readable", problemDetail));
    }

    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex thrown exception.
     * @return error message with 500 status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(final Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(createErrorMessage(ex, "Something Happened", null));
    }

    private ErrorMessage createErrorMessage(final Exception ex, final String message, final ProblemDetail problemDetail) {
        ErrorMessage errorMessage = ErrorMessage.builder()
            .message(message)
            .error(String.valueOf(ex.getClass()))
            .problemDetail(problemDetail)
            .build();
        if (Environment.isDevEnvironment(environment)) {
            errorMessage.setStackTrace(ex.getStackTrace());
            errorMessage.setThrowable(ThrowableInfo.from(ex.getCause()));
        }
        return errorMessage;
    }
}
