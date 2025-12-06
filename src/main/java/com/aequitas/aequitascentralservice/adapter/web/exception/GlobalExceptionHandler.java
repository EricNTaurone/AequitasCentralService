package com.aequitas.aequitascentralservice.adapter.web.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions into RFC 7807 {@link ProblemDetail} responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation failures triggered by bean validation.
     *
     * @param ex thrown exception.
     * @return problem detail payload.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(final MethodArgumentNotValidException ex) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problemDetail.setTitle("VALIDATION");
        final Map<String, String> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("details", details);
        return problemDetail;
    }

    /**
     * Handles illegal arguments which usually indicate a 400 response.
     *
     * @param ex thrown exception.
     * @return problem detail.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(final IllegalArgumentException ex) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("VALIDATION");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    /**
     * Handles illegal state exceptions raised during business logic evaluation.
     *
     * @param ex thrown exception.
     * @return problem detail.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(final IllegalStateException ex) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("CONFLICT");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    /**
     * Handles authorization failures triggered by business rules.
     *
     * @param ex thrown exception.
     * @return problem detail with 403 status.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(final AccessDeniedException ex) {
        final ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        detail.setTitle("FORBIDDEN");
        detail.setDetail(ex.getMessage());
        return detail;
    }
}
