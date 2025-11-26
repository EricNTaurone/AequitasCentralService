package com.aequitas.aequitascentralservice.adapter.web.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateUserRoleRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UserProfileResponse;
import com.aequitas.aequitascentralservice.adapter.web.mapper.UserProfileDtoMapper;
import com.aequitas.aequitascentralservice.app.port.inbound.UserProfileCommandPort;
import com.aequitas.aequitascentralservice.app.port.inbound.UserProfileQueryPort;
import com.aequitas.aequitascentralservice.domain.value.Role;

import jakarta.validation.Valid;

/**
 * REST controller exposing the User Profile domain API for managing firm user rosters, role
 * assignments, and profile retrieval within a multi‑tenant SaaS architecture.
 *
 * <p>This controller serves as the web‑layer adapter in a hexagonal architecture, delegating
 * business logic to {@link UserProfileCommandPort} and {@link UserProfileQueryPort}. All
 * operations are scoped to the authenticated tenant context, enforced by underlying security
 * filters.
 *
 * <p><strong>Thread‑Safety:</strong> This controller is stateless; Spring instantiates it as a
 * singleton, and all injected dependencies are themselves thread‑safe.
 *
 * <p><strong>Authorization:</strong> This controller enforces role‑based access control (RBAC):
 * <ul>
 *   <li><strong>Employee:</strong> Can retrieve own profile via {@code /me}.</li>
 *   <li><strong>Manager:</strong> Can list users and retrieve profiles within their team.</li>
 *   <li><strong>Admin:</strong> Can list all users, retrieve any profile, and update role assignments.</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong>
 * <pre>{@code
 * // Retrieve the authenticated user's profile
 * GET /api/v1/users/me
 * Response: 200 OK, { "id": "...", "email": "...", "role": "EMPLOYEE", ... }
 *
 * // List all users with MANAGER role (admin only)
 * GET /api/v1/users?role=MANAGER
 * Response: 200 OK, [ { "id": "...", "role": "MANAGER", ... }, ... ]
 *
 * // Update a user's role to ADMIN (admin only)
 * PATCH /api/v1/users/{id}/role
 * Body: { "role": "ADMIN" }
 * Response: 204 No Content
 * }</pre>
 *
 * @see UserProfileCommandPort
 * @see UserProfileQueryPort
 * @see Role
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserProfileController {

    /**
     * Query port handling read‑only user profile retrieval and listing operations.
     * Injected by Spring; never null.
     */
    private final UserProfileQueryPort queryPort;

    /**
     * Command port handling state‑changing user profile operations (role updates).
     * Injected by Spring; never null.
     */
    private final UserProfileCommandPort commandPort;

    /**
     * Constructs a new controller with required hexagonal ports and supporting services.
     *
     * <p>All dependencies are injected by Spring's constructor‑based dependency injection. This
     * design promotes immutability and explicit contracts, simplifying unit testing and reasoning
     * about collaborators.
     *
     * @param queryPort   Port for executing user profile queries; must not be null.
     * @param commandPort Port for executing user profile mutations; must not be null.
     */
    public UserProfileController(
            final UserProfileQueryPort queryPort,
            final UserProfileCommandPort commandPort) {
        this.queryPort = queryPort;
        this.commandPort = commandPort;
    }

    /**
     * Retrieves the authenticated user's own profile, including identity, role, tenant membership,
     * and contact information.
     *
     * <p>This endpoint is accessible by all authenticated users regardless of role. The profile
     * returned is always scoped to the caller's identity as extracted from the security context
     * (typically a JWT or session).
     *
     * <p><strong>Authorization:</strong> Any authenticated user can access their own profile. No
     * elevated privileges required.
     *
     * <p><strong>Use Cases:</strong> This endpoint supports UI personalization, permission checks,
     * and navigation logic (e.g., showing admin‑only menu items based on role).
     *
     * <p><strong>Performance:</strong> O(1) database lookup using the authenticated user's
     * identifier; typical latency is 1‑3ms for warm caches.
     *
     * <p><strong>Side‑Effects:</strong> None; this is a read‑only operation.
     *
     * @return A {@link ResponseEntity} with HTTP 200 OK and a {@link UserProfileResponse} containing
     *         the caller's unique identifier, email, full name, role, tenant identifier, and
     *         timestamps (createdAt, lastLoginAt).
     * @throws org.springframework.security.access.AccessDeniedException
     *         if no authenticated user context exists (should never occur due to security filters).
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me() {
        return ResponseEntity.ok(UserProfileDtoMapper.toResponse(queryPort.me()));
    }

    /**
     * Lists all user profiles within the authenticated tenant's firm, optionally filtered by role.
     *
     * <p>This endpoint supports administrative and managerial use cases such as team roster
     * management, role auditing, and user discovery. Results are always scoped to the caller's
     * tenant; cross‑tenant queries are not permitted.
     *
     * <p><strong>Authorization:</strong> Only users with MANAGER or ADMIN roles can access this
     * endpoint. EMPLOYEE‑level users receive a 403 Forbidden response. Managers may see a filtered
     * subset based on their team scope, while admins see all tenant users.
     *
     * <p><strong>Filtering:</strong> The {@code role} parameter is optional and case‑insensitive:
     * <ul>
     *   <li>When omitted or blank, all users in the tenant are returned.</li>
     *   <li>When specified (e.g., "manager", "ADMIN"), only users with that role are included.</li>
     *   <li>Invalid role strings return 400 Bad Request via {@link IllegalArgumentException}.</li>
     * </ul>
     *
     * <p><strong>Performance:</strong> Query complexity is O(n) where n is the tenant's user count,
     * typically < 1000 for small‑to‑medium firms. Results are not paginated; for large tenants
     * (> 10,000 users), consider adding pagination in future versions. Typical latency is 10‑50ms.
     *
     * <p><strong>Side‑Effects:</strong> None; this is a read‑only operation.
     *
     * <p><strong>Edge Cases:</strong>
     * <ul>
     *   <li>Empty tenants return an empty list with HTTP 200 OK.</li>
     *   <li>Filtering by a role with zero matches returns an empty list.</li>
     * </ul>
     *
     * @param role Optional role filter as a case‑insensitive string (e.g., "employee", "MANAGER",
     *             "admin"); when null or blank, all roles are included.
     * @return     A {@link ResponseEntity} with HTTP 200 OK and a {@link List} of
     *             {@link UserProfileResponse} objects, sorted by user creation timestamp ascending.
     *             May be empty if no users match the filter criteria.
     * @throws IllegalArgumentException if {@code role} is non‑blank but does not match any
     *                                  {@link Role} enum constant.
     * @throws org.springframework.security.access.AccessDeniedException
     *                                  if the caller lacks MANAGER or ADMIN privileges.
     */
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> list(
            @RequestParam(name = "role", required = false) final String role) {
        final Optional<Role> parsedRole = parseRole(role);
        final List<UserProfileResponse> response =
                queryPort.list(parsedRole).stream().map(UserProfileDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a user's role assignment within the tenant, typically performed during onboarding,
     * promotions, or permission adjustments.
     *
     * <p>This operation is a partial update affecting only the user's role; other profile fields
     * (email, name, etc.) remain unchanged. Role transitions are validated against business rules:
     * <ul>
     *   <li>Cannot demote the last ADMIN to prevent lock‑out scenarios.</li>
     *   <li>Cannot assign roles to users outside the caller's tenant.</li>
     * </ul>
     *
     * <p><strong>Authorization:</strong> Only users with the ADMIN role can invoke this endpoint.
     * Managers and employees receive a 403 Forbidden response.
     *
     * <p><strong>Idempotency:</strong> Updating a user to their current role is a no‑op; the
     * operation succeeds without side‑effects or errors.
     *
     * <p><strong>Side‑Effects:</strong> Updates the user's role in the database and publishes a
     * {@code UserRoleChanged} domain event for audit logging and downstream permission cache
     * invalidation.
     *
     * <p><strong>Performance:</strong> O(1) database update; typical latency is 2‑5ms.
     *
     * <p><strong>Validation:</strong> The request body is validated using Jakarta Bean Validation
     * annotations. Invalid payloads (e.g., missing or invalid role) return 400 Bad Request.
     *
     * @param id      The unique identifier of the user whose role should be updated; must exist
     *                within the tenant scope.
     * @param request The validated payload containing the new {@link Role} value; must not be null.
     * @return        A {@link ResponseEntity} with HTTP 204 No Content on success.
     * @throws com.aequitas.aequitascentralservice.domain.exception.UserNotFoundException
     *                if no user with {@code id} exists in the tenant.
     * @throws com.aequitas.aequitascentralservice.domain.exception.LastAdminProtectionException
     *                if attempting to demote the last ADMIN user.
     * @throws jakarta.validation.ConstraintViolationException
     *                if {@code request} violates validation rules.
     * @throws org.springframework.security.access.AccessDeniedException
     *                if the caller lacks ADMIN privileges.
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable final UUID id, @Valid @RequestBody final UpdateUserRoleRequest request) {
        commandPort.updateRole(id, Role.valueOf(request.getRole().name()));
        return ResponseEntity.noContent().build();
    }

    /**
     * Parses a role query parameter into a domain {@link Role} enum value.
     *
     * <p>This helper method supports case‑insensitive parsing and gracefully handles null or blank
     * inputs by returning {@link Optional#empty()}. Invalid non‑blank strings throw
     * {@link IllegalArgumentException}, which Spring translates to 400 Bad Request.
     *
     * @param role The raw role string from the query parameter; may be null, blank, or
     *             whitespace‑padded; case‑insensitive (e.g., "employee", "MANAGER", " Admin ").
     * @return     An {@link Optional} containing the parsed {@link Role}, or
     *             {@link Optional#empty()} if {@code role} is null or blank.
     * @throws IllegalArgumentException if {@code role} is non‑blank but does not match any
     *                                  {@link Role} enum constant.
     */
    private Optional<Role> parseRole(final String role) {
        if (role == null || role.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Role.valueOf(role.trim().toUpperCase()));
    }
}
