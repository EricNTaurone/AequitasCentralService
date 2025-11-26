package com.aequitas.aequitascentralservice.app.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aequitas.aequitascentralservice.app.port.inbound.FirmCommandPort;
import com.aequitas.aequitascentralservice.app.port.inbound.FirmQueryPort;
import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.FirmRepositoryPort;
import com.aequitas.aequitascentralservice.domain.command.CreateFirmCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateFirmCommand;
import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import com.aequitas.aequitascentralservice.domain.value.Address;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;

import jakarta.annotation.Resource;

/**
 * Implements firm management operations with RBAC enforcement.
 *
 * <p>
 * This service orchestrates firm-related business logic, delegating persistence
 * operations to {@link FirmRepositoryPort} while enforcing role-based access
 * control for administrative operations.
 *
 * <p>
 * <strong>Authorization Model:</strong>
 * <ul>
 * <li><strong>All Users:</strong> Can retrieve their own firm information via
 * {@link #getCurrentUserFirm()}</li>
 * <li><strong>ADMIN Role:</strong> Can list all firms, retrieve any firm by ID,
 * create new firms, and update firm details</li>
 * <li><strong>Cross-Tenant Restrictions:</strong> Admins can only update firms
 * within their own tenant to maintain isolation</li>
 * </ul>
 *
 * <p>
 * <strong>Transaction Management:</strong> Write operations are marked with
 * {@code @Transactional} to ensure atomicity. Read operations use
 * {@code readOnly=true} for optimization.
 *
 * <p>
 * <strong>Thread-Safety:</strong> This service is stateless and thread-safe;
 * Spring manages it as a singleton.
 *
 * @see FirmCommandPort
 * @see FirmQueryPort
 * @see FirmRepositoryPort
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
public class FirmService implements FirmQueryPort, FirmCommandPort {

    @Resource
    private final FirmRepositoryPort repositoryPort;
    @Resource
    private final CurrentUserPort currentUserPort;
    @Resource
    private final ClockPort clockPort;

    public FirmService(
            final FirmRepositoryPort repositoryPort,
            final CurrentUserPort currentUserPort,
            final ClockPort clockPort) {
        this.repositoryPort = repositoryPort;
        this.currentUserPort = currentUserPort;
        this.clockPort = clockPort;
    }

    @Override
    public Firm getCurrentUserFirm() {
        final CurrentUser currentUser = currentUserPort.currentUser();
        return repositoryPort
                .findById(currentUser.firmId())
                .orElseThrow(() -> new IllegalArgumentException("Firm not found"));
    }

    @Override
    public Optional<Firm> findById(final UUID id) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        ensureAdminRole(currentUser);
        return repositoryPort.findById(id);
    }

    @Override
    public PageResult<Firm> list(final PageRequest pageRequest) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        ensureAdminRole(currentUser);
        return repositoryPort.list(pageRequest);
    }

    @Override
    @Transactional
    public UUID create(final CreateFirmCommand command) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        ensureAdminRole(currentUser);

        final Instant now = clockPort.now();
        final Firm firm = Firm.create(command.name(), command.address(), now);
        return repositoryPort.save(firm).getId();
    }

    @Override
    @Transactional
    public void update(final UUID id, final UpdateFirmCommand command) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        ensureAdminRole(currentUser);

        final Firm firm = repositoryPort
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Firm not found"));

        // Only admins from the same firm can update it
        if (!firm.getId().equals(currentUser.firmId())) {
            throw new AccessDeniedException("Cannot update a different firm");
        }

        final String newName = command.name().orElse(null);
        final Address newAddress = command.address().orElse(null);
        final Instant now = clockPort.now();

        final Firm updatedFirm = firm.update(newName, newAddress, now);
        repositoryPort.save(updatedFirm);
    }

    private void ensureAdminRole(final CurrentUser currentUser) {
        if (currentUser.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this operation");
        }
    }
}
