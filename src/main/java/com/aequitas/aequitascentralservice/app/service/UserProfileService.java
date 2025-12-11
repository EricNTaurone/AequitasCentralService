package com.aequitas.aequitascentralservice.app.service;

import com.aequitas.aequitascentralservice.app.port.inbound.UserProfileCommandPort;
import com.aequitas.aequitascentralservice.app.port.inbound.UserProfileQueryPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.UserProfileRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements user roster queries and administrative commands.
 */
@Service
@Transactional(readOnly = true)
public class UserProfileService implements UserProfileQueryPort, UserProfileCommandPort {

    private final UserProfileRepositoryPort repositoryPort;
    private final CurrentUserPort currentUserPort;

    public UserProfileService(
        final UserProfileRepositoryPort repositoryPort, final CurrentUserPort currentUserPort) {
        this.repositoryPort = repositoryPort;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public UserProfile me() {
        final CurrentUser currentUser = currentUserPort.currentUser();
        return repositoryPort
            .findById(currentUser.userId(), currentUser.firmId())
            .orElseThrow(() -> new IllegalArgumentException("User profile not found"));
    }

    @Override
    public UserProfile createUserProfile(UserProfile userProfile) {
        return repositoryPort.save(userProfile);
    }

    @Override
    public UserProfile findByAuthenticationId(UUID authenticationId) {
        return repositoryPort.findByAuthenticationId(authenticationId)
            .orElseThrow(() -> new IllegalArgumentException("User profile not found"));
    }

    @Override
    public List<UserProfile> list(final Optional<Role> role) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        ensureRosterPermission(currentUser);
        return role
            .map(
                value ->
                    repositoryPort.findByFirmIdAndRole(currentUser.firmId(), value))
            .orElseGet(() -> repositoryPort.findByFirmId(currentUser.firmId()));
    }

    @Override
    @Transactional
    public void updateRole(final UUID userId, final Role newRole) {
        final CurrentUser currentUser = currentUserPort.currentUser();
        if (currentUser.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can change roles");
        }
        final UserProfile profile = repositoryPort
            .findById(userId, currentUser.firmId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        final UserProfile updated = new UserProfile(profile.id(), profile.authenticationId(), profile.firmId(), profile.email(), newRole);
        repositoryPort.save(updated);
    }

    private void ensureRosterPermission(final CurrentUser currentUser) {
        if (currentUser.role() == Role.EMPLOYEE) {
            throw new AccessDeniedException("Employees cannot list firm users");
        }
    }
}
