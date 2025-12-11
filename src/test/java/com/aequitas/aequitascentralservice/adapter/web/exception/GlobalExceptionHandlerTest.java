package com.aequitas.aequitascentralservice.adapter.web.exception;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    private void setEnvironment(String env) throws Exception {
        Field environmentField = GlobalExceptionHandler.class.getDeclaredField("environment");
        environmentField.setAccessible(true);
        environmentField.set(handler, env);
    }

    // ========== handleValidation Tests ==========

    @Test
    void GIVEN_validationExceptionWithSingleFieldErrorInDevEnv_WHEN_handleValidation_THEN_returnsErrorMessageWithProblemDetail() throws Exception {
        // GIVEN
        setEnvironment("dev");
        final FieldError fieldError = new FieldError("objectName", "fieldName", "must not be null");
        final BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleValidation(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Validation failed");
        assertThat(result.getError()).isEqualTo("class org.springframework.web.bind.MethodArgumentNotValidException");
        assertThat(result.getStackTrace()).isNotNull();
        assertThat(result.getStackTrace()).isNotEmpty();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("VALIDATION");
        assertThat(problemDetail.getProperties()).isNotNull();
        
        @SuppressWarnings("unchecked")
        final Map<String, String> details = (Map<String, String>) problemDetail.getProperties().get("details");
        assertThat(details).isNotNull();
        assertThat(details).hasSize(1);
        assertThat(details).containsEntry("fieldName", "must not be null");
    }

    @Test
    void GIVEN_validationExceptionWithMultipleFieldErrorsInProdEnv_WHEN_handleValidation_THEN_returnsErrorMessageWithoutStackTrace() throws Exception {
        // GIVEN
        setEnvironment("prod");
        final FieldError fieldError1 = new FieldError("objectName", "email", "must be a valid email");
        final FieldError fieldError2 = new FieldError("objectName", "age", "must be positive");
        final FieldError fieldError3 = new FieldError("objectName", "name", "must not be blank");
        
        final BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2, fieldError3));
        
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleValidation(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Validation failed");
        assertThat(result.getError()).isEqualTo("class org.springframework.web.bind.MethodArgumentNotValidException");
        assertThat(result.getStackTrace()).isNull();
        assertThat(result.getThrowable()).isNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("VALIDATION");
        
        @SuppressWarnings("unchecked")
        final Map<String, String> details = (Map<String, String>) problemDetail.getProperties().get("details");
        assertThat(details).isNotNull();
        assertThat(details).hasSize(3);
        assertThat(details).containsEntry("email", "must be a valid email");
        assertThat(details).containsEntry("age", "must be positive");
        assertThat(details).containsEntry("name", "must not be blank");
    }

    @Test
    void GIVEN_validationExceptionWithNoFieldErrors_WHEN_handleValidation_THEN_returnsEmptyDetailsMap() throws Exception {
        // GIVEN
        setEnvironment("gamma");
        final BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleValidation(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Validation failed");
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("VALIDATION");
        
        @SuppressWarnings("unchecked")
        final Map<String, String> details = (Map<String, String>) problemDetail.getProperties().get("details");
        assertThat(details).isNotNull();
        assertThat(details).isEmpty();
    }

    // ========== handleIllegalArgument Tests ==========

    @Test
    void GIVEN_illegalArgumentExceptionInDevEnv_WHEN_handleIllegalArgument_THEN_returnsErrorMessageWithStackTrace() throws Exception {
        // GIVEN
        setEnvironment("dev");
        final String errorMessage = "Invalid parameter provided";
        final IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleIllegalArgument(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Invalid argument");
        assertThat(result.getError()).isEqualTo("class java.lang.IllegalArgumentException");
        assertThat(result.getStackTrace()).isNotNull();
        assertThat(result.getStackTrace()).isNotEmpty();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("VALIDATION");
        assertThat(problemDetail.getDetail()).isEqualTo(errorMessage);
    }

    @Test
    void GIVEN_illegalArgumentExceptionWithNullMessageInProdEnv_WHEN_handleIllegalArgument_THEN_returnsErrorMessageWithoutStackTrace() throws Exception {
        // GIVEN
        setEnvironment("prod");
        final IllegalArgumentException ex = new IllegalArgumentException((String) null);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleIllegalArgument(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Invalid argument");
        assertThat(result.getStackTrace()).isNull();
        assertThat(result.getThrowable()).isNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("VALIDATION");
        assertThat(problemDetail.getDetail()).isNull();
    }

    @Test
    void GIVEN_illegalArgumentExceptionWithEmptyMessage_WHEN_handleIllegalArgument_THEN_returnsBadRequestWithEmptyDetail() throws Exception {
        // GIVEN
        setEnvironment("beta");
        final IllegalArgumentException ex = new IllegalArgumentException("");

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleIllegalArgument(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Invalid argument");
        assertThat(result.getStackTrace()).isNotNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("VALIDATION");
        assertThat(problemDetail.getDetail()).isEmpty();
    }

    // ========== handleIllegalState Tests ==========

    @Test
    void GIVEN_illegalStateExceptionInDevEnv_WHEN_handleIllegalState_THEN_returnsErrorMessageWithConflict() throws Exception {
        // GIVEN
        setEnvironment("dev");
        final String errorMessage = "Resource already exists";
        final IllegalStateException ex = new IllegalStateException(errorMessage);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleIllegalState(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Conflict state");
        assertThat(result.getError()).isEqualTo("class java.lang.IllegalStateException");
        assertThat(result.getStackTrace()).isNotNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("CONFLICT");
        assertThat(problemDetail.getDetail()).isEqualTo(errorMessage);
    }

    @Test
    void GIVEN_illegalStateExceptionWithNullMessageInProdEnv_WHEN_handleIllegalState_THEN_returnsErrorMessageWithoutStackTrace() throws Exception {
        // GIVEN
        setEnvironment("prod");
        final IllegalStateException ex = new IllegalStateException((String) null);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleIllegalState(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Conflict state");
        assertThat(result.getStackTrace()).isNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("CONFLICT");
        assertThat(problemDetail.getDetail()).isNull();
    }

    @Test
    void GIVEN_illegalStateExceptionWithEmptyMessage_WHEN_handleIllegalState_THEN_returnsConflictWithEmptyDetail() throws Exception {
        // GIVEN
        setEnvironment("gamma");
        final IllegalStateException ex = new IllegalStateException("");

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleIllegalState(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Conflict state");
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("CONFLICT");
        assertThat(problemDetail.getDetail()).isEmpty();
    }

    // ========== handleAccessDenied Tests ==========

    @Test
    void GIVEN_accessDeniedExceptionInDevEnv_WHEN_handleAccessDenied_THEN_returnsErrorMessageWithForbidden() throws Exception {
        // GIVEN
        setEnvironment("dev");
        final String errorMessage = "User does not have permission to access this resource";
        final AccessDeniedException ex = new AccessDeniedException(errorMessage);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleAccessDenied(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Access denied");
        assertThat(result.getError()).isEqualTo("class org.springframework.security.access.AccessDeniedException");
        assertThat(result.getStackTrace()).isNotNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problemDetail.getTitle()).isEqualTo("FORBIDDEN");
        assertThat(problemDetail.getDetail()).isEqualTo(errorMessage);
    }

    @Test
    void GIVEN_accessDeniedExceptionWithNullMessageInProdEnv_WHEN_handleAccessDenied_THEN_returnsErrorMessageWithoutStackTrace() throws Exception {
        // GIVEN
        setEnvironment("prod");
        final AccessDeniedException ex = new AccessDeniedException(null);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleAccessDenied(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Access denied");
        assertThat(result.getStackTrace()).isNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problemDetail.getTitle()).isEqualTo("FORBIDDEN");
        assertThat(problemDetail.getDetail()).isNull();
    }

    @Test
    void GIVEN_accessDeniedExceptionWithEmptyMessage_WHEN_handleAccessDenied_THEN_returnsForbiddenWithEmptyDetail() throws Exception {
        // GIVEN
        setEnvironment("beta");
        final AccessDeniedException ex = new AccessDeniedException("");

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleAccessDenied(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Access denied");
        assertThat(result.getStackTrace()).isNotNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problemDetail.getTitle()).isEqualTo("FORBIDDEN");
        assertThat(problemDetail.getDetail()).isEmpty();
    }

    // ========== handleHttpMessageNotReadable Tests ==========

    @Test
    void GIVEN_httpMessageNotReadableExceptionWithCauseInDevEnv_WHEN_handleHttpMessageNotReadable_THEN_returnsErrorMessageWithCauseMessage() throws Exception {
        // GIVEN
        setEnvironment("dev");
        final String causeMessage = "Cannot deserialize value of type `Status` from String";
        final Throwable cause = new RuntimeException(causeMessage);
        final HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            "JSON parse error", cause, mock(HttpInputMessage.class));

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleHttpMessageNotReadable(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Message not readable");
        assertThat(result.getError()).isEqualTo("class org.springframework.http.converter.HttpMessageNotReadableException");
        assertThat(result.getStackTrace()).isNotNull();
        assertThat(result.getThrowable()).isNotNull();
        assertThat(result.getThrowable().getType()).isEqualTo(cause.getClass().getName());
        assertThat(result.getThrowable().getMessage()).isEqualTo(cause.getMessage());
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("VALIDATION");
        assertThat(problemDetail.getDetail()).isEqualTo(causeMessage);
    }

    @Test
    void GIVEN_httpMessageNotReadableExceptionWithoutCauseInProdEnv_WHEN_handleHttpMessageNotReadable_THEN_returnsErrorMessageWithExceptionMessage() throws Exception {
        // GIVEN
        setEnvironment("prod");
        final String errorMessage = "Required request body is missing";
        final HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            errorMessage, (Throwable) null, mock(HttpInputMessage.class));

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleHttpMessageNotReadable(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Message not readable");
        assertThat(result.getStackTrace()).isNull();
        assertThat(result.getThrowable()).isNull();
        
        final ProblemDetail problemDetail = result.getProblemDetail();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("VALIDATION");
        assertThat(problemDetail.getDetail()).isEqualTo(errorMessage);
    }

    // ========== handleException (Generic) Tests ==========

    @Test
    void GIVEN_genericExceptionInDevEnvironment_WHEN_handleException_THEN_returnsErrorMessageWithStackTraceAndNullProblemDetail() throws Exception {
        // GIVEN
        setEnvironment("dev");
        final Throwable cause = new IllegalArgumentException("Root cause");
        final Exception ex = new RuntimeException("Something went wrong", cause);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleException(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Something Happened");
        assertThat(result.getError()).isEqualTo("class java.lang.RuntimeException");
        assertThat(result.getStackTrace()).isNotNull();
        assertThat(result.getStackTrace()).isNotEmpty();
        assertThat(result.getThrowable()).isNotNull();
        assertThat(result.getThrowable().getType()).isEqualTo(cause.getClass().getName());
        assertThat(result.getThrowable().getMessage()).isEqualTo(cause.getMessage());
        assertThat(result.getProblemDetail()).isNull();
    }

    @Test
    void GIVEN_genericExceptionInBetaEnvironment_WHEN_handleException_THEN_returnsErrorMessageWithStackTraceAndCause() throws Exception {
        // GIVEN
        setEnvironment("beta");
        final Throwable cause = new NullPointerException("Null value encountered");
        final Exception ex = new RuntimeException("Unexpected error", cause);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleException(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Something Happened");
        assertThat(result.getError()).isEqualTo("class java.lang.RuntimeException");
        assertThat(result.getStackTrace()).isNotNull();
        assertThat(result.getStackTrace()).isNotEmpty();
        assertThat(result.getThrowable()).isNotNull();
        assertThat(result.getThrowable().getType()).isEqualTo(cause.getClass().getName());
        assertThat(result.getThrowable().getMessage()).isEqualTo(cause.getMessage());
        assertThat(result.getProblemDetail()).isNull();
    }

    @Test
    void GIVEN_genericExceptionInGammaEnvironment_WHEN_handleException_THEN_returnsErrorMessageWithoutStackTraceAndCause() throws Exception {
        // GIVEN
        setEnvironment("gamma");
        final Throwable cause = new IllegalStateException("State error");
        final Exception ex = new RuntimeException("Error occurred", cause);

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleException(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Something Happened");
        assertThat(result.getError()).isEqualTo("class java.lang.RuntimeException");
        assertThat(result.getStackTrace()).isNull();
        assertThat(result.getThrowable()).isNull();
        assertThat(result.getProblemDetail()).isNull();
    }

    @Test
    void GIVEN_genericExceptionInProdEnvironment_WHEN_handleException_THEN_returnsErrorMessageWithoutStackTraceAndCause() throws Exception {
        // GIVEN
        setEnvironment("prod");
        final Exception ex = new RuntimeException("Production error");

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleException(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Something Happened");
        assertThat(result.getError()).isEqualTo("class java.lang.RuntimeException");
        assertThat(result.getStackTrace()).isNull();
        assertThat(result.getThrowable()).isNull();
        assertThat(result.getProblemDetail()).isNull();
    }

    @Test
    void GIVEN_genericExceptionWithNullCauseInDevEnvironment_WHEN_handleException_THEN_returnsErrorMessageWithStackTraceAndNullCause() throws Exception {
        // GIVEN
        setEnvironment("dev");
        final Exception ex = new RuntimeException("No cause exception");

        // WHEN
        final ResponseEntity<ErrorMessage> response = handler.handleException(ex);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        final ErrorMessage result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Something Happened");
        assertThat(result.getError()).isEqualTo("class java.lang.RuntimeException");
        assertThat(result.getStackTrace()).isNotNull();
        assertThat(result.getStackTrace()).isNotEmpty();
        assertThat(result.getThrowable()).isNull();
        assertThat(result.getProblemDetail()).isNull();
    }
}
