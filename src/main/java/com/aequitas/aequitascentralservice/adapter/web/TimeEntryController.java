package com.aequitas.aequitascentralservice.adapter.web;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aequitas.aequitascentralservice.adapter.web.dto.CreateTimeEntryRequest;
import com.aequitas.aequitascentralservice.adapter.web.dto.IdResponse;
import com.aequitas.aequitascentralservice.adapter.web.dto.PageResponse;
import com.aequitas.aequitascentralservice.adapter.web.dto.TimeEntryResponse;
import com.aequitas.aequitascentralservice.adapter.web.dto.UpdateTimeEntryRequest;
import com.aequitas.aequitascentralservice.app.port.inbound.TimeEntryCommandPort;
import com.aequitas.aequitascentralservice.app.port.inbound.TimeEntryQueryPort;
import com.aequitas.aequitascentralservice.app.service.IdempotencyService;
import com.aequitas.aequitascentralservice.domain.model.TimeEntryFilter;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;
import com.aequitas.aequitascentralservice.domain.value.IdempotencyOperation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * REST controller exposing the Time Entry domain API for creating, updating, submitting, and
 * approving employee time entries within a multi‑tenant SaaS architecture.
 *
 * <p>This controller serves as the web‑layer adapter in a hexagonal architecture, delegating
 * business logic to {@link TimeEntryCommandPort} and {@link TimeEntryQueryPort}. All operations
 * are scoped to the authenticated tenant context, enforced by underlying security filters.
 *
 * <p><strong>Thread‑Safety:</strong> This controller is stateless; Spring instantiates it as a
 * singleton, and all injected dependencies are themselves thread‑safe.
 *
 * <p><strong>Idempotency:</strong> Mutation endpoints (create, approve) accept an optional
 * {@code Idempotency-Key} header to prevent duplicate operations caused by client retries. The
 * {@link IdempotencyService} ensures that identical keys produce identical outcomes without
 * side‑effects.
 *
 * <p><strong>Usage Example:</strong>
 * <pre>{@code
 * // Create a new time entry with idempotency protection
 * POST /api/v1/entries
 * Headers: Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
 * Body: { "customerId": "...", "projectId": "...", "hours": 8.0, ... }
 * Response: 201 Created, { "id": "..." }
 *
 * // Search entries with filters and pagination
 * GET /api/v1/entries?projectId=...&status=SUBMITTED&limit=50&cursor=...
 * Response: 200 OK, { "items": [...], "nextCursor": "...", "hasMore": true }
 * }</pre>
 *
 * @see TimeEntryCommandPort
 * @see TimeEntryQueryPort
 * @see IdempotencyService
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/entries")
@Validated
public class TimeEntryController {

    /**
     * Command port handling state‑changing time entry operations (create, update, submit, approve).
     * Injected by Spring; never null.
     */
    private final TimeEntryCommandPort commandPort;

    /**
     * Query port handling read‑only time entry retrieval and search operations.
     * Injected by Spring; never null.
     */
    private final TimeEntryQueryPort queryPort;

    /**
     * Mapper converting between web‑layer DTOs and domain commands/models.
     * Injected by Spring; never null.
     */
    private final TimeEntryDtoMapper mapper;

    /**
     * Service enforcing idempotency semantics for mutation operations using client‑supplied keys.
     * Injected by Spring; never null.
     */
    private final IdempotencyService idempotencyService;

    /**
     * Constructs a new controller with required hexagonal ports and supporting services.
     *
     * <p>All dependencies are injected by Spring's constructor‑based dependency injection. This
     * design promotes immutability and explicit contracts, simplifying unit testing and reasoning
     * about collaborators.
     *
     * @param commandPort   Port for executing time entry mutations; must not be null.
     * @param queryPort     Port for executing time entry queries; must not be null.
     * @param mapper        Mapper for DTO ↔ domain conversions; must not be null.
     * @param idempotencyService Service managing idempotency keys; must not be null.
     */
    public TimeEntryController(
            final TimeEntryCommandPort commandPort,
            final TimeEntryQueryPort queryPort,
            final TimeEntryDtoMapper mapper,
            final IdempotencyService idempotencyService) {
        this.commandPort = commandPort;
        this.queryPort = queryPort;
        this.mapper = mapper;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Creates a new time entry in DRAFT status within the authenticated tenant's workspace.
     *
     * <p>This operation is idempotent when an {@code Idempotency-Key} header is supplied. If the
     * same key is reused, the original entry's identifier is returned without creating a duplicate.
     * Keys are scoped to the tenant and operation type, preventing cross‑contamination.
     *
     * <p><strong>Validation:</strong> The request body is validated using Jakarta Bean Validation
     * annotations. Invalid payloads return 400 Bad Request with structured error details.
     *
     * <p><strong>Side‑Effects:</strong> Persists a new time entry aggregate root and publishes a
     * domain event for downstream systems (e.g., billing, analytics).
     *
     * <p><strong>Performance:</strong> O(1) primary operation; idempotency lookup incurs an
     * additional database query (~1‑5ms).
     *
     * @param idempotencyKey Optional client‑supplied UUID in header form; when null, no idempotency
     *                       protection is applied. Should be a fresh UUID for each logical request.
     * @param request        The validated time entry creation payload containing customer, project,
     *                       hours, date, and description; must not be null.
     * @return               A {@link ResponseEntity} with HTTP 201 Created status and an
     *                       {@link IdResponse} containing the newly created (or existing, if
     *                       idempotent) entry identifier.
     * @throws jakarta.validation.ConstraintViolationException if {@code request} violates validation rules.
     * @throws IllegalStateException if the tenant context cannot be resolved (should never occur
     *                               in production due to security filters).
     */
    @PostMapping
    public ResponseEntity<IdResponse> create(
            @RequestHeader(name = "Idempotency-Key", required = false) final String idempotencyKey,
            @Valid @RequestBody final CreateTimeEntryRequest request) {
        final UUID id =
                idempotencyService.execute(
                        idempotencyKey,
                        IdempotencyOperation.TIME_ENTRY_CREATE,
                        () -> commandPort.create(mapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(new IdResponse(id));
    }

    /**
     * Applies a partial update to an existing time entry using JSON Patch or merge semantics.
     *
     * <p>Only mutable fields (hours, date, description, etc.) can be updated; immutable identifiers
     * and tenant associations are rejected. The entry must be in DRAFT status; submitted or approved
     * entries cannot be modified via this endpoint.
     *
     * <p><strong>Concurrency:</strong> Updates are not idempotent by default. Concurrent patches to
     * the same entry may result in last‑write‑wins behavior. Optimistic locking can be added via
     * version headers if required.
     *
     * <p><strong>Validation:</strong> The patch payload is validated; invalid fields return 400.
     *
     * <p><strong>Side‑Effects:</strong> Persists field changes and updates the entry's
     * {@code lastModifiedAt} timestamp. Does not publish domain events unless business‑critical
     * fields change.
     *
     * @param id      The unique identifier of the time entry to update; must exist within the
     *                tenant scope.
     * @param request The validated patch payload containing fields to update; must not be null.
     * @return        A {@link ResponseEntity} with HTTP 204 No Content on success.
     * @throws com.aequitas.aequitascentralservice.domain.exception.TimeEntryNotFoundException
     *                if no entry with {@code id} exists in the tenant.
     * @throws com.aequitas.aequitascentralservice.domain.exception.InvalidStateTransitionException
     *                if the entry is not in DRAFT status.
     * @throws jakarta.validation.ConstraintViolationException if {@code request} violates validation rules.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable final UUID id, @Valid @RequestBody final UpdateTimeEntryRequest request) {
        commandPort.update(id, mapper.toCommand(request));
        return ResponseEntity.noContent().build();
    }

    /**
     * Submits a time entry for managerial approval, transitioning it from DRAFT to SUBMITTED status.
     *
     * <p>Once submitted, the entry becomes immutable and cannot be updated via the {@code PATCH}
     * endpoint. This state transition triggers a notification to the assigned approver (typically
     * a project manager or team lead).
     *
     * <p><strong>Idempotency:</strong> Submitting an already‑submitted entry is a no‑op; the
     * operation succeeds without error or duplicate side‑effects.
     *
     * <p><strong>Business Rules:</strong> Only the entry owner (or a user with elevated privileges)
     * can submit. Entries with invalid hours (e.g., negative, exceeding daily limits) are rejected
     * during prior validation phases.
     *
     * <p><strong>Side‑Effects:</strong> Updates the entry status and publishes a
     * {@code TimeEntrySubmitted} domain event for downstream notification and audit systems.
     *
     * @param id The unique identifier of the time entry to submit; must exist and be in DRAFT status.
     * @return   A {@link ResponseEntity} with HTTP 204 No Content on success.
     * @throws com.aequitas.aequitascentralservice.domain.exception.TimeEntryNotFoundException
     *               if no entry with {@code id} exists in the tenant.
     * @throws com.aequitas.aequitascentralservice.domain.exception.InvalidStateTransitionException
     *               if the entry is not in DRAFT status or the caller lacks ownership.
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submit(@PathVariable final UUID id) {
        commandPort.submit(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Approves a submitted time entry, transitioning it to APPROVED status and making it eligible
     * for payroll or billing processing.
     *
     * <p>This operation is idempotent when an {@code Idempotency-Key} header is supplied. Repeated
     * approval requests with the same key do not alter the entry's state or trigger duplicate
     * downstream actions (e.g., billing events).
     *
     * <p><strong>Authorization:</strong> Only users with managerial or approver roles can invoke
     * this endpoint. The underlying command port enforces role‑based access control.
     *
     * <p><strong>Business Rules:</strong> The entry must be in SUBMITTED status. Approving a DRAFT
     * or already‑APPROVED entry results in an {@code InvalidStateTransitionException}.
     *
     * <p><strong>Side‑Effects:</strong> Updates the entry status, records the approver's identity
     * and timestamp, and publishes a {@code TimeEntryApproved} domain event for payroll integration.
     *
     * @param id             The unique identifier of the time entry to approve; must exist and be
     *                       in SUBMITTED status.
     * @param idempotencyKey Optional client‑supplied UUID in header form; when null, no idempotency
     *                       protection is applied. Recommended for reliable approval workflows.
     * @return               A {@link ResponseEntity} with HTTP 204 No Content on success.
     * @throws com.aequitas.aequitascentralservice.domain.exception.TimeEntryNotFoundException
     *                       if no entry with {@code id} exists in the tenant.
     * @throws com.aequitas.aequitascentralservice.domain.exception.InvalidStateTransitionException
     *                       if the entry is not in SUBMITTED status.
     * @throws org.springframework.security.access.AccessDeniedException
     *                       if the caller lacks approver privileges.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable final UUID id,
            @RequestHeader(name = "Idempotency-Key", required = false) final String idempotencyKey) {
        idempotencyService.execute(
                idempotencyKey,
                IdempotencyOperation.TIME_ENTRY_APPROVE,
                () -> {
                    commandPort.approve(id);
                    return id;
                });
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a single time entry by its unique identifier, scoped to the authenticated tenant.
     *
     * <p>This endpoint supports detailed inspection of entry fields, including audit metadata
     * (created/modified timestamps, approver identity) and associated customer/project references.
     *
     * <p><strong>Authorization:</strong> Users can retrieve their own entries; managers can retrieve
     * any entry within their team or tenant scope, enforced by the query port.
     *
     * <p><strong>Performance:</strong> O(1) database lookup with tenant‑scoped indexing; typical
     * latency is 1‑5ms for warm caches.
     *
     * @param id The unique identifier of the time entry to retrieve; must exist within the tenant.
     * @return   A {@link ResponseEntity} containing:
     *           <ul>
     *             <li>HTTP 200 OK with a {@link TimeEntryResponse} body if the entry exists.</li>
     *             <li>HTTP 404 Not Found if no entry with {@code id} exists in the tenant scope.</li>
     *           </ul>
     */
    @GetMapping("/{id}")
    public ResponseEntity<TimeEntryResponse> findById(@PathVariable final UUID id) {
        return queryPort.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Searches and retrieves a paginated list of time entries within the authenticated tenant,
     * optionally filtered by customer, project, status, and owner.
     *
     * <p>This endpoint supports cursor‑based pagination for stable iteration over large result sets,
     * even when entries are concurrently added or modified. The cursor is an opaque string encoding
     * the last‑seen identifier and sort key.
     *
     * <p><strong>Filtering:</strong> All filter parameters are optional and combine with AND logic:
     * <ul>
     *   <li>{@code customerId}: Restrict results to entries for a specific customer.</li>
     *   <li>{@code projectId}: Restrict results to entries for a specific project.</li>
     *   <li>{@code status}: Restrict results to a specific lifecycle status (DRAFT, SUBMITTED, APPROVED).</li>
     *   <li>{@code ownerId}: (Managers only) Restrict results to entries owned by a specific user.</li>
     * </ul>
     *
     * <p><strong>Authorization:</strong> Users retrieve only their own entries unless they have
     * managerial privileges, in which case they can query across team members via {@code ownerId}.
     *
     * <p><strong>Performance:</strong> Query complexity is O(log n + k) where n is the tenant's
     * total entry count and k is the page size, leveraging composite indexes on tenant + filter
     * columns. Typical latency is 5‑20ms for pages up to 100 items.
     *
     * <p><strong>Edge Cases:</strong>
     * <ul>
     *   <li>Invalid {@code status} strings return 400 Bad Request.</li>
     *   <li>An invalid {@code cursor} returns 400 Bad Request.</li>
     *   <li>Empty result sets return an empty {@code items} array with {@code hasMore: false}.</li>
     * </ul>
     *
     * @param customerId Optional customer UUID filter; when null, no customer filter is applied.
     * @param projectId  Optional project UUID filter; when null, no project filter is applied.
     * @param status     Optional status filter as a case‑insensitive string (e.g., "draft", "SUBMITTED");
     *                   when null or blank, all statuses are included.
     * @param ownerId    Optional owner UUID filter; typically used by managers to query subordinates;
     *                   when null, defaults to the caller's own entries unless the caller is a manager.
     * @param limit      The maximum number of items to return; must be between 1 and 100 inclusive;
     *                   defaults to 20.
     * @param cursor     Optional pagination cursor from a previous response's {@code nextCursor};
     *                   when null, returns the first page.
     * @return           A {@link ResponseEntity} with HTTP 200 OK and a {@link PageResponse} containing:
     *                   <ul>
     *                     <li>{@code items}: A list of {@link TimeEntryResponse} objects (may be empty).</li>
     *                     <li>{@code nextCursor}: Opaque cursor for the next page; null if no more results.</li>
     *                     <li>{@code totalItems}: Approximate total count (may be cached or estimated for performance).</li>
     *                     <li>{@code hasMore}: Boolean indicating whether additional pages exist.</li>
     *                   </ul>
     * @throws IllegalArgumentException if {@code status} cannot be parsed to a valid {@link EntryStatus}.
     * @throws jakarta.validation.ConstraintViolationException if {@code limit} violates bounds.
     */
    @GetMapping
    public ResponseEntity<PageResponse<TimeEntryResponse>> search(
            @RequestParam(name = "customerId", required = false) final UUID customerId,
            @RequestParam(name = "projectId", required = false) final UUID projectId,
            @RequestParam(name = "status", required = false) final String status,
            @RequestParam(name = "ownerId", required = false) final UUID ownerId,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(100) final int limit,
            @RequestParam(name = "cursor", required = false) final String cursor) {
        final TimeEntryFilter filter =
                new TimeEntryFilter(
                        Optional.ofNullable(customerId),
                        Optional.ofNullable(projectId),
                        parseStatus(status),
                        Optional.ofNullable(ownerId));
        final PageRequest pageRequest = new PageRequest(limit, cursor);
        final var page = queryPort.search(filter, pageRequest);
        final var responseItems = page.items().stream().map(mapper::toResponse).toList();
        final PageResponse<TimeEntryResponse> response =
                new PageResponse<>(responseItems, page.nextCursor(), page.totalItems(), page.hasMore());
        return ResponseEntity.ok(response);
    }

    /**
     * Parses a status query parameter into a domain {@link EntryStatus} enum value.
     *
     * <p>This helper method supports case‑insensitive parsing and gracefully handles null or blank
     * inputs by returning {@link Optional#empty()}. Invalid non‑blank strings throw
     * {@link IllegalArgumentException}, which Spring translates to 400 Bad Request.
     *
     * @param status The raw status string from the query parameter; may be null, blank, or
     *               whitespace‑padded; case‑insensitive (e.g., "draft", "SUBMITTED", " Approved ").
     * @return       An {@link Optional} containing the parsed {@link EntryStatus}, or
     *               {@link Optional#empty()} if {@code status} is null or blank.
     * @throws IllegalArgumentException if {@code status} is non‑blank but does not match any
     *                                  {@link EntryStatus} enum constant.
     */
    private Optional<EntryStatus> parseStatus(final String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(EntryStatus.valueOf(status.trim().toUpperCase()));
    }
}
