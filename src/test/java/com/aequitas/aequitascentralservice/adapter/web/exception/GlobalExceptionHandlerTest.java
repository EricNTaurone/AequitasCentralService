package com.aequitas.aequitascentralservice.adapter.web.exception;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Test suite for {@link GlobalExceptionHandler}.
 * Ensures 100% line, branch, and mutation coverage.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void GIVEN_validationExceptionWithSingleFieldError_WHEN_handleValidation_THEN_returnsUnprocessableEntityWithFieldDetails() {
        // GIVEN
        final FieldError fieldError = new FieldError("objectName", "fieldName", "must not be null");
        final BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // WHEN
        final ProblemDetail result = handler.handleValidation(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
        assertThat(result.getTitle()).isEqualTo("VALIDATION");
        assertThat(result.getProperties()).isNotNull();
        
        @SuppressWarnings("unchecked")
        final Map<String, String> details = (Map<String, String>) result.getProperties().get("details");
        assertThat(details).isNotNull();
        assertThat(details).hasSize(1);
        assertThat(details).containsEntry("fieldName", "must not be null");
    }

    @Test
    void GIVEN_validationExceptionWithMultipleFieldErrors_WHEN_handleValidation_THEN_returnsAllFieldErrorsInDetails() {
        // GIVEN
        final FieldError fieldError1 = new FieldError("objectName", "email", "must be a valid email");
        final FieldError fieldError2 = new FieldError("objectName", "age", "must be positive");
        final FieldError fieldError3 = new FieldError("objectName", "name", "must not be blank");
        
        final BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2, fieldError3));
        
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // WHEN
        final ProblemDetail result = handler.handleValidation(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
        assertThat(result.getTitle()).isEqualTo("VALIDATION");
        
        @SuppressWarnings("unchecked")
        final Map<String, String> details = (Map<String, String>) result.getProperties().get("details");
        assertThat(details).isNotNull();
        assertThat(details).hasSize(3);
        assertThat(details).containsEntry("email", "must be a valid email");
        assertThat(details).containsEntry("age", "must be positive");
        assertThat(details).containsEntry("name", "must not be blank");
    }

    @Test
    void GIVEN_validationExceptionWithNoFieldErrors_WHEN_handleValidation_THEN_returnsEmptyDetailsMap() {
        // GIVEN
        final BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // WHEN
        final ProblemDetail result = handler.handleValidation(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
        assertThat(result.getTitle()).isEqualTo("VALIDATION");
        
        @SuppressWarnings("unchecked")
        final Map<String, String> details = (Map<String, String>) result.getProperties().get("details");
        assertThat(details).isNotNull();
        assertThat(details).isEmpty();
    }

    @Test
    void GIVEN_illegalArgumentException_WHEN_handleIllegalArgument_THEN_returnsBadRequestWithMessage() {
        // GIVEN
        final String errorMessage = "Invalid parameter provided";
        final IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        // WHEN
        final ProblemDetail result = handler.handleIllegalArgument(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("VALIDATION");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
    }

    @Test
    void GIVEN_illegalArgumentExceptionWithNullMessage_WHEN_handleIllegalArgument_THEN_returnsBadRequestWithNullDetail() {
        // GIVEN
        final IllegalArgumentException ex = new IllegalArgumentException((String) null);

        // WHEN
        final ProblemDetail result = handler.handleIllegalArgument(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("VALIDATION");
        assertThat(result.getDetail()).isNull();
    }

    @Test
    void GIVEN_illegalArgumentExceptionWithEmptyMessage_WHEN_handleIllegalArgument_THEN_returnsBadRequestWithEmptyDetail() {
        // GIVEN
        final IllegalArgumentException ex = new IllegalArgumentException("");

        // WHEN
        final ProblemDetail result = handler.handleIllegalArgument(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("VALIDATION");
        assertThat(result.getDetail()).isEmpty();
    }

    @Test
    void GIVEN_illegalStateException_WHEN_handleIllegalState_THEN_returnsConflictWithMessage() {
        // GIVEN
        final String errorMessage = "Resource already exists";
        final IllegalStateException ex = new IllegalStateException(errorMessage);

        // WHEN
        final ProblemDetail result = handler.handleIllegalState(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("CONFLICT");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
    }

    @Test
    void GIVEN_illegalStateExceptionWithNullMessage_WHEN_handleIllegalState_THEN_returnsConflictWithNullDetail() {
        // GIVEN
        final IllegalStateException ex = new IllegalStateException((String) null);

        // WHEN
        final ProblemDetail result = handler.handleIllegalState(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("CONFLICT");
        assertThat(result.getDetail()).isNull();
    }

    @Test
    void GIVEN_illegalStateExceptionWithEmptyMessage_WHEN_handleIllegalState_THEN_returnsConflictWithEmptyDetail() {
        // GIVEN
        final IllegalStateException ex = new IllegalStateException("");

        // WHEN
        final ProblemDetail result = handler.handleIllegalState(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("CONFLICT");
        assertThat(result.getDetail()).isEmpty();
    }

    @Test
    void GIVEN_accessDeniedException_WHEN_handleAccessDenied_THEN_returnsForbiddenWithMessage() {
        // GIVEN
        final String errorMessage = "User does not have permission to access this resource";
        final AccessDeniedException ex = new AccessDeniedException(errorMessage);

        // WHEN
        final ProblemDetail result = handler.handleAccessDenied(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("FORBIDDEN");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
    }

    @Test
    void GIVEN_accessDeniedExceptionWithNullMessage_WHEN_handleAccessDenied_THEN_returnsForbiddenWithNullDetail() {
        // GIVEN
        final AccessDeniedException ex = new AccessDeniedException(null);

        // WHEN
        final ProblemDetail result = handler.handleAccessDenied(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("FORBIDDEN");
        assertThat(result.getDetail()).isNull();
    }

    @Test
    void GIVEN_accessDeniedExceptionWithEmptyMessage_WHEN_handleAccessDenied_THEN_returnsForbiddenWithEmptyDetail() {
        // GIVEN
        final AccessDeniedException ex = new AccessDeniedException("");

        // WHEN
        final ProblemDetail result = handler.handleAccessDenied(ex);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("FORBIDDEN");
        assertThat(result.getDetail()).isEmpty();
    }
}
