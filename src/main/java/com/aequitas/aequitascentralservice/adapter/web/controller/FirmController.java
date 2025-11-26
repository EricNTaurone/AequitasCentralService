package com.aequitas.aequitascentralservice.adapter.web.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.CreateFirmRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.FirmPageResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.FirmResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.IdResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateFirmRequest;
import com.aequitas.aequitascentralservice.adapter.web.mapper.FirmDtoMapper;
import com.aequitas.aequitascentralservice.app.port.inbound.FirmCommandPort;
import com.aequitas.aequitascentralservice.app.port.inbound.FirmQueryPort;
import com.aequitas.aequitascentralservice.domain.command.CreateFirmCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateFirmCommand;
import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * REST controller exposing the Firm domain API for managing firm information
 * and settings within a multi-tenant SaaS architecture.
 *
 * <p>
 * This controller serves as the web-layer adapter in a hexagonal architecture,
 * delegating business logic to {@link FirmCommandPort} and
 * {@link FirmQueryPort}. Operations are protected by role-based access control.
 *
 * <p>
 * <strong>Thread-Safety:</strong> This controller is stateless; Spring
 * instantiates it as a singleton, and all injected dependencies are themselves
 * thread-safe.
 *
 * <p>
 * <strong>Authorization:</strong> This controller enforces role-based access
 * control:
 * <ul>
 * <li><strong>All Users:</strong> Can retrieve their own firm via
 * {@code /me}.</li>
 * <li><strong>Admin:</strong> Can retrieve any firm, list all firms, create new
 * firms, and update firm details.</li>
 * </ul>
 *
 * @see FirmCommandPort
 * @see FirmQueryPort
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/firms")
@Validated
@Tag(name = "Firms", description = "Operations for managing firm information and settings")
public class FirmController {

    /**
     * Query port handling read-only firm retrieval operations. Injected by
     * Spring; never null.
     */
    private final FirmQueryPort queryPort;

    /**
     * Command port handling state-changing firm operations. Injected by Spring;
     * never null.
     */
    private final FirmCommandPort commandPort;

    /**
     * Constructs a new controller with required hexagonal ports.
     *
     * <p>
     * All dependencies are injected by Spring's constructor-based dependency
     * injection. This design promotes immutability and explicit contracts,
     * simplifying unit testing.
     *
     * @param queryPort Port for executing firm queries; must not be null.
     * @param commandPort Port for executing firm mutations; must not be null.
     */
    public FirmController(
            final FirmQueryPort queryPort,
            final FirmCommandPort commandPort) {
        this.queryPort = queryPort;
        this.commandPort = commandPort;
    }

    /**
     * Retrieves the authenticated user's firm information.
     *
     * <p>
     * This endpoint is accessible by all authenticated users regardless of
     * role. The firm returned is always scoped to the caller's tenant.
     *
     * @return A {@link ResponseEntity} with HTTP 200 OK and a
     * {@link FirmResponse} containing the firm's details.
     */
    @GetMapping("/me")
    @Operation(summary = "Get authenticated user's firm",
            description = "Retrieves the authenticated user's firm information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Firm retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Firm not found")
    })
    public ResponseEntity<FirmResponse> getCurrentUserFirm() {
        final Firm firm = queryPort.getCurrentUserFirm();
        return ResponseEntity.ok(FirmDtoMapper.toResponse(firm));
    }

    /**
     * Retrieves a specific firm by identifier.
     *
     * <p>
     * <strong>Authorization:</strong> Only users with ADMIN role can access
     * this endpoint.
     *
     * @param id firm identifier.
     * @return A {@link ResponseEntity} with HTTP 200 OK and a
     * {@link FirmResponse}, or HTTP 404 if not found.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get firm by ID",
            description = "Retrieves detailed information about a specific firm")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Firm found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Firm not found")
    })
    public ResponseEntity<FirmResponse> getFirmById(
            @Parameter(description = "Firm identifier") @PathVariable final UUID id) {
        return queryPort.findById(id)
                .map(FirmDtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a paginated list of all firms.
     *
     * <p>
     * <strong>Authorization:</strong> Only users with ADMIN role can access
     * this endpoint.
     *
     * @param limit Maximum number of items to return (default 20, max 100).
     * @param cursor Pagination cursor from previous response.
     * @return A {@link ResponseEntity} with HTTP 200 OK and a
     * {@link FirmPageResponse}.
     */
    @GetMapping
    @Operation(summary = "List firms with pagination",
            description = "Retrieves a paginated list of all firms")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful list operation"),
        @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<FirmPageResponse> listFirms(
            @Parameter(description = "Maximum number of items to return")
            @RequestParam(name = "limit", required = false, defaultValue = "20")
            @Min(1) @Max(100) final Integer limit,
            @Parameter(description = "Pagination cursor from previous response")
            @RequestParam(name = "cursor", required = false) final String cursor) {
        final PageRequest pageRequest = new PageRequest(limit, cursor);
        final PageResult<Firm> result = queryPort.list(pageRequest);

        final FirmPageResponse response = new FirmPageResponse();
        response.setItems(result.items().stream()
                .map(FirmDtoMapper::toResponse)
                .toList());
        response.setNextCursor(result.nextCursor());
        response.setTotal(result.totalItems());
        response.setHasMore(result.hasMore());

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new firm with the provided details.
     *
     * <p>
     * <strong>Authorization:</strong> Only users with ADMIN role can access
     * this endpoint.
     *
     * @param request creation request containing firm details.
     * @return A {@link ResponseEntity} with HTTP 201 Created and an
     * {@link IdResponse} containing the new firm's identifier.
     */
    @PostMapping
    @Operation(summary = "Create a new firm",
            description = "Creates a new firm with the provided name and address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Firm created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Firm already exists")
    })
    public ResponseEntity<IdResponse> createFirm(
            @Valid @RequestBody final CreateFirmRequest request) {
        final CreateFirmCommand command = FirmDtoMapper.toCommand(request);
        final UUID firmId = commandPort.create(command);

        final IdResponse response = new IdResponse();
        response.setId(firmId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing firm's mutable attributes.
     *
     * <p>
     * <strong>Authorization:</strong> Only admins from the same firm can update
     * it.
     *
     * @param id firm identifier.
     * @param request update request containing new values.
     * @return A {@link ResponseEntity} with HTTP 204 No Content on success.
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Update firm details",
            description = "Updates a firm's mutable attributes (name and address)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Firm updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Firm not found")
    })
    public ResponseEntity<Void> updateFirm(
            @Parameter(description = "Firm identifier") @PathVariable final UUID id,
            @Valid @RequestBody final UpdateFirmRequest request) {
        final UpdateFirmCommand command = FirmDtoMapper.toCommand(request);
        commandPort.update(id, command);
        return ResponseEntity.noContent().build();
    }
}
