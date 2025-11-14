package com.aequitas.aequitascentralservice.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.UserProfileRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

/**
 * Tests for {@link UserProfileService}.
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserProfileRepositoryPort repositoryPort;
    @Mock private CurrentUserPort currentUserPort;

    private UserProfileService service;
    private CurrentUser adminUser;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(repositoryPort, currentUserPort);
        adminUser = new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), Role.ADMIN);
    }

    @Test
    @DisplayName("me should return the current user's profile")
    void meReturnsProfile() {
        when(currentUserPort.currentUser()).thenReturn(adminUser);
        final UserProfile expected =
                new UserProfile(adminUser.userId(), adminUser.firmId(), "admin@example.com", Role.ADMIN);
        when(repositoryPort.findById(adminUser.userId(), adminUser.firmId()))
                .thenReturn(Optional.of(expected));

        assertThat(service.me()).isEqualTo(expected);
    }

    @Test
    @DisplayName("list should throw when called by employee")
    void listForbiddenForEmployee() {
        final CurrentUser employee =
                new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), Role.EMPLOYEE);
        when(currentUserPort.currentUser()).thenReturn(employee);

        assertThatThrownBy(() -> service.list(Optional.empty()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("list should delegate to repository for managers/admins")
    void listReturnsProfiles() {
        when(currentUserPort.currentUser()).thenReturn(adminUser);
        final List<UserProfile> profiles =
                List.of(new UserProfile(UUID.randomUUID(), adminUser.firmId(), "a@b.com", Role.MANAGER));
        when(repositoryPort.findByFirmId(adminUser.firmId())).thenReturn(profiles);

        assertThat(service.list(Optional.empty())).containsExactlyElementsOf(profiles);
    }

    @Test
    @DisplayName("updateRole should only allow admins")
    void updateRoleAdminOnly() {
        final CurrentUser manager =
                new CurrentUser(UUID.randomUUID(), adminUser.firmId(), Role.MANAGER);
        when(currentUserPort.currentUser()).thenReturn(manager);

        assertThatThrownBy(() -> service.updateRole(UUID.randomUUID(), Role.ADMIN))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("updateRole should persist role changes")
    void updateRolePersists() {
        when(currentUserPort.currentUser()).thenReturn(adminUser);
        final UUID targetUser = UUID.randomUUID();
        final UserProfile existing =
                new UserProfile(targetUser, adminUser.firmId(), "lawyer@example.com", Role.EMPLOYEE);
        when(repositoryPort.findById(targetUser, adminUser.firmId()))
                .thenReturn(Optional.of(existing));

        service.updateRole(targetUser, Role.MANAGER);

        verify(repositoryPort).save(new UserProfile(targetUser, adminUser.firmId(), existing.email(), Role.MANAGER));
    }
}
