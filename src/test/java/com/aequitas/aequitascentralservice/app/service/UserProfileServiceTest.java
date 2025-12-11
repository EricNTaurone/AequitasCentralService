package com.aequitas.aequitascentralservice.app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.UserProfileRepositoryPort;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;

/**
 * Tests for {@link UserProfileService}.
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_FIRM_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TEST_TARGET_USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final String TEST_EMAIL = "admin@example.com";
    private static final String TEST_EMAIL_2 = "lawyer@example.com";
    private static final String TEST_EMAIL_3 = "a@b.com";

    @Captor
    private ArgumentCaptor<UserProfile> userProfileCaptor;

    @Mock
    private UserProfileRepositoryPort repositoryPort;
    @Mock
    private CurrentUserPort currentUserPort;

    @InjectMocks
    private UserProfileService service;

    @Test
    void GIVEN_currentUser_WHEN_me_THEN_returnCurrentUserProfile() {
        // GIVEN
        CurrentUser currentUser = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.ADMIN);
        UserProfile expectedProfile = new UserProfile(TEST_USER_ID, UUID.randomUUID(), TEST_FIRM_ID, TEST_EMAIL, Role.ADMIN);
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(TEST_USER_ID, TEST_FIRM_ID))
                .thenReturn(Optional.of(expectedProfile));

        // WHEN
        UserProfile result = service.me();

        // THEN
        assertEquals(expectedProfile, result);
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, times(1)).findById(TEST_USER_ID, TEST_FIRM_ID);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_currentUserProfileNotFound_WHEN_me_THEN_throwIllegalArgumentException() {
        // GIVEN
        CurrentUser currentUser = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.ADMIN);
        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(TEST_USER_ID, TEST_FIRM_ID))
                .thenReturn(Optional.empty());

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.me());

        // THEN
        assertEquals("User profile not found", exception.getMessage());
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, times(1)).findById(TEST_USER_ID, TEST_FIRM_ID);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_employeeUser_WHEN_list_THEN_throwAccessDeniedException() {
        // GIVEN
        CurrentUser employee = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.EMPLOYEE);
        when(currentUserPort.currentUser()).thenReturn(employee);

        // WHEN
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.list(Optional.empty()));

        // THEN
        assertEquals("Employees cannot list firm users", exception.getMessage());
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, never()).findByFirmId(TEST_FIRM_ID);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_adminUserWithoutRoleFilter_WHEN_list_THEN_returnAllProfiles() {
        // GIVEN
        CurrentUser admin = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.ADMIN);
        UserProfile profile1 = new UserProfile(UUID.randomUUID(), UUID.randomUUID(), TEST_FIRM_ID, TEST_EMAIL_3, Role.MANAGER);
        List<UserProfile> profiles = List.of(profile1);
        when(currentUserPort.currentUser()).thenReturn(admin);
        when(repositoryPort.findByFirmId(TEST_FIRM_ID)).thenReturn(profiles);

        // WHEN
        List<UserProfile> result = service.list(Optional.empty());

        // THEN
        assertEquals(1, result.size());
        assertEquals(profile1, result.get(0));
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, times(1)).findByFirmId(TEST_FIRM_ID);
        verify(repositoryPort, never()).findByFirmIdAndRole(TEST_FIRM_ID, Role.MANAGER);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_managerUserWithRoleFilter_WHEN_list_THEN_returnFilteredProfiles() {
        // GIVEN
        CurrentUser manager = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.MANAGER);
        UserProfile profile1 = new UserProfile(UUID.randomUUID(), UUID.randomUUID(), TEST_FIRM_ID, TEST_EMAIL_3, Role.EMPLOYEE);
        List<UserProfile> profiles = List.of(profile1);
        when(currentUserPort.currentUser()).thenReturn(manager);
        when(repositoryPort.findByFirmIdAndRole(TEST_FIRM_ID, Role.EMPLOYEE)).thenReturn(profiles);

        // WHEN
        List<UserProfile> result = service.list(Optional.of(Role.EMPLOYEE));

        // THEN
        assertEquals(1, result.size());
        assertEquals(profile1, result.get(0));
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, times(1)).findByFirmIdAndRole(TEST_FIRM_ID, Role.EMPLOYEE);
        verify(repositoryPort, never()).findByFirmId(TEST_FIRM_ID);
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_managerUser_WHEN_updateRole_THEN_throwAccessDeniedException() {
        // GIVEN
        CurrentUser manager = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.MANAGER);
        when(currentUserPort.currentUser()).thenReturn(manager);

        // WHEN
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.updateRole(TEST_TARGET_USER_ID, Role.ADMIN));

        // THEN
        assertEquals("Only admins can change roles", exception.getMessage());
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, never()).findById(TEST_TARGET_USER_ID, TEST_FIRM_ID);
        verify(repositoryPort, never()).save(userProfileCaptor.capture());
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_employeeUser_WHEN_updateRole_THEN_throwAccessDeniedException() {
        // GIVEN
        CurrentUser employee = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.EMPLOYEE);
        when(currentUserPort.currentUser()).thenReturn(employee);

        // WHEN
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.updateRole(TEST_TARGET_USER_ID, Role.MANAGER));

        // THEN
        assertEquals("Only admins can change roles", exception.getMessage());
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, never()).findById(TEST_TARGET_USER_ID, TEST_FIRM_ID);
        verify(repositoryPort, never()).save(userProfileCaptor.capture());
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_adminUserAndExistingProfile_WHEN_updateRole_THEN_persistUpdatedRole() {
        // GIVEN
        CurrentUser admin = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.ADMIN);
        UserProfile existing = new UserProfile(TEST_TARGET_USER_ID, UUID.randomUUID(), TEST_FIRM_ID, TEST_EMAIL_2, Role.EMPLOYEE);
        when(currentUserPort.currentUser()).thenReturn(admin);
        when(repositoryPort.findById(TEST_TARGET_USER_ID, TEST_FIRM_ID))
                .thenReturn(Optional.of(existing));

        // WHEN
        service.updateRole(TEST_TARGET_USER_ID, Role.MANAGER);

        // THEN
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, times(1)).findById(TEST_TARGET_USER_ID, TEST_FIRM_ID);
        verify(repositoryPort, times(1)).save(userProfileCaptor.capture());
        UserProfile savedProfile = userProfileCaptor.getValue();
        assertEquals(TEST_TARGET_USER_ID, savedProfile.id());
        assertEquals(TEST_FIRM_ID, savedProfile.firmId());
        assertEquals(TEST_EMAIL_2, savedProfile.email());
        assertEquals(Role.MANAGER, savedProfile.role());
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }

    @Test
    void GIVEN_adminUserAndNonExistentProfile_WHEN_updateRole_THEN_throwIllegalArgumentException() {
        // GIVEN
        CurrentUser admin = new CurrentUser(TEST_USER_ID, TEST_FIRM_ID, Role.ADMIN);
        when(currentUserPort.currentUser()).thenReturn(admin);
        when(repositoryPort.findById(TEST_TARGET_USER_ID, TEST_FIRM_ID))
                .thenReturn(Optional.empty());

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateRole(TEST_TARGET_USER_ID, Role.MANAGER));

        // THEN
        assertEquals("User not found", exception.getMessage());
        verify(currentUserPort, times(1)).currentUser();
        verify(repositoryPort, times(1)).findById(TEST_TARGET_USER_ID, TEST_FIRM_ID);
        verify(repositoryPort, never()).save(userProfileCaptor.capture());
        verifyNoMoreInteractions(currentUserPort, repositoryPort);
    }
}
