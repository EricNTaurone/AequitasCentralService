package com.aequitas.aequitascentralservice.adapter.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.AuthResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.SignInRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.SignUpRequest;
import com.aequitas.aequitascentralservice.adapter.web.mapper.AuthenticationDtoMapper;
import com.aequitas.aequitascentralservice.app.port.inbound.AuthenticationCommandPort;
import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.value.Role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * REST controller exposing Supabase-backed authentication flows for user
 * registration and login.
 * <p>
 * This controller manages the authentication lifecycle by delegating to the
 * authentication command port, which integrates with Supabase as the identity
 * provider. It handles user sign-up with firm association and role assignment,
 * as well as email/password-based sign-in.
 * <p>
 * All endpoints validate incoming requests using Jakarta Bean Validation and
 * return standardized {@link AuthResponse} DTOs containing session tokens and
 * user metadata.
 * <p>
 * <b>Thread Safety:</b> This controller is stateless and safe for concurrent
 * invocation by multiple threads.
 * <p>
 * <b>Usage Example:</b> null {@code
 * POST /api/v1/auth/signup
 * {
 *   "firmId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
 *   "email": "user@example.com",
 *   "password": "SecurePass123!",
 *   "role": "ANALYST"
 * }
 *
 * @see AuthenticationCommandPort
 * @see AuthResponse
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthenticationController {

    /**
     * Port to the authentication domain service that orchestrates user
     * registration and sign-in flows.
     * <p>
     * This dependency is injected via constructor and is immutable after
     * construction.
     */
    @Resource
    private final AuthenticationCommandPort authenticationCommandPort;

    /**
     * Constructs a new authentication controller with the given command port.
     * <p>
     * Intended for dependency injection by the Spring container. The command
     * port handles the core authentication logic and Supabase integration.
     *
     * @param authenticationCommandPort The authentication command port that
     * executes sign-up and sign-in operations; must not be {@code null}.
     * @throws NullPointerException If {@code authenticationCommandPort} is
     * {@code null}.
     */
    public AuthenticationController(final AuthenticationCommandPort authenticationCommandPort) {
        this.authenticationCommandPort = authenticationCommandPort;
    }

    /**
     * Registers a new user in Supabase and associates them with a firm and
     * role.
     * <p>
     * This endpoint creates a new user account with the provided email and
     * password, then links the user to the specified firm identifier and
     * assigns the requested role. Upon successful registration, Supabase
     * generates session tokens (access and refresh) which are returned in the
     * response.
     * <p>
     * The request payload is validated for email format, password strength,
     * firm ID presence, and valid role enumeration. If any validation fails, a
     * 400 Bad Request is returned.
     * <p>
     * <b>Idempotence:</b> This operation is <b>not idempotent</b>. Repeated
     * calls with the same email will fail if the user already exists in
     * Supabase.
     *
     * @param request The sign-up request containing firmId (UUID), email (RFC
     * 5322 compliant), password (minimum 8 characters), and role (one of ADMIN,
     * ANALYST, VIEWER, etc.); must not be {@code null} and must pass
     * {@code @Valid} constraints.
     * @return A {@link ResponseEntity} with HTTP 201 Created status and an
     * {@link AuthResponse} body containing the access token, refresh token,
     * token expiry timestamp, and user metadata (userId, email, firmId, role).
     * @throws org.springframework.web.bind.MethodArgumentNotValidException If
     * the request fails validation.
     * @throws
     * com.aequitas.aequitascentralservice.domain.exception.AuthenticationException
     * If Supabase rejects the sign-up due to duplicate email, weak password, or
     * service unavailability.
     * @throws
     * com.aequitas.aequitascentralservice.domain.exception.FirmNotFoundException
     * If the specified firmId does not exist in the system.
     */
    @PostMapping("/signup")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account in Supabase with firm association and role assignment"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "User successfully registered",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request payload (validation failure)",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Firm not found",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "409",
                description = "User with this email already exists",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Supabase service error or internal server error",
                content = @Content
        )
    })
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody final SignUpRequest request) {
        final var session = authenticationCommandPort.signUp(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthenticationDtoMapper.toResponse(session));
    }

    /**
     * Authenticates an existing user with email and password credentials.
     * <p>
     * This endpoint validates the provided email and password against Supabase
     * and, if successful, returns a new session containing fresh access and
     * refresh tokens. The access token can be used for subsequent API calls
     * requiring authentication.
     * <p>
     * The request is validated for email format and password presence. If
     * credentials are invalid, Supabase returns an authentication error which
     * is propagated to the caller.
     * <p>
     * <b>Idempotence:</b> This operation is <b>not idempotent</b>. Each
     * successful call generates a new session with distinct tokens, though
     * previous sessions remain valid until expiry.
     *
     * @param request The sign-in request containing email (must match an
     * existing user) and password (plaintext, validated against the stored
     * hash); must not be {@code null} and must pass {@code @Valid} constraints.
     * @return A {@link ResponseEntity} with HTTP 200 OK status and an
     * {@link AuthResponse} body containing the access token, refresh token,
     * token expiry timestamp, and user metadata (userId, email, firmId, role).
     * @throws org.springframework.web.bind.MethodArgumentNotValidException If
     * the request fails validation.
     * @throws
     * com.aequitas.aequitascentralservice.domain.exception.AuthenticationException
     * If the email does not exist, the password is incorrect, or Supabase
     * service is unavailable.
     */
    @PostMapping("/signin")
    @Operation(
            summary = "Authenticate an existing user",
            description = "Validates email and password credentials and returns a new session with access tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "User successfully authenticated",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request payload (validation failure)",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Invalid credentials (wrong email or password)",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Supabase service error or internal server error",
                content = @Content
        )
    })
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody final SignInRequest request) {
        final var session
                = authenticationCommandPort.signIn(new SignInCommand(request.getEmail(), request.getPassword()));
        return ResponseEntity.ok(AuthenticationDtoMapper.toResponse(session));
    }

    /**
     * Transforms a web-layer sign-up request DTO into a domain command object.
     * <p>
     * This method maps the incoming HTTP request payload to the domain model
     * expected by the authentication service. It converts the DTO's role enum
     * to the domain {@link Role} enum using name-based matching.
     * <p>
     * <b>Complexity:</b> O(1) — simple field mapping and enum conversion.
     *
     * @param request The validated sign-up request from the client; must not be
     * {@code null}.
     * @return A {@link SignUpCommand} containing firmId, email, password, and
     * the mapped domain role.
     * @throws IllegalArgumentException If the role name in the request does not
     * match any domain {@link Role} constant (though this should be prevented
     * by DTO validation).
     */
    private SignUpCommand toCommand(final SignUpRequest request) {
        return new SignUpCommand(
                request.getFirmId(),
                request.getEmail(),
                request.getPassword(),
                Role.valueOf(request.getRole().toString()));
    }
}
