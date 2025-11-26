package com.aequitas.aequitascentralservice.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.UserProfileEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.UserProfileJpaRepository;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.Role;

@ExtendWith(MockitoExtension.class)
class UserProfileRepositoryAdapterTest {

    @Mock
    private UserProfileJpaRepository repository;

    @InjectMocks
    private UserProfileRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<UserProfileEntity> entityCaptor;

    // ==================== findById() Tests ====================

    @Test
    void GIVEN_existingProfile_WHEN_findById_THEN_returnsProfile() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();

        final UserProfileEntity entity = UserProfileEntity.builder()
                .id(id)
                .firmId(firmId)
                .email("user@example.com")
                .role(Role.EMPLOYEE)
                .build();

        when(repository.findByIdAndFirmId(id, firmId)).thenReturn(Optional.of(entity));

        // WHEN
        final Optional<UserProfile> result = adapter.findById(id, firmId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(id);
        assertThat(result.get().firmId()).isEqualTo(firmId);
        assertThat(result.get().email()).isEqualTo("user@example.com");
        assertThat(result.get().role()).isEqualTo(Role.EMPLOYEE);

        verify(repository).findByIdAndFirmId(id, firmId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_nonExistingProfile_WHEN_findById_THEN_returnsEmpty() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();

        when(repository.findByIdAndFirmId(id, firmId)).thenReturn(Optional.empty());

        // WHEN
        final Optional<UserProfile> result = adapter.findById(id, firmId);

        // THEN
        assertThat(result).isEmpty();

        verify(repository).findByIdAndFirmId(id, firmId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_profileFromDifferentFirm_WHEN_findById_THEN_returnsEmpty() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID wrongFirmId = UUID.randomUUID();

        when(repository.findByIdAndFirmId(id, wrongFirmId)).thenReturn(Optional.empty());

        // WHEN
        final Optional<UserProfile> result = adapter.findById(id, wrongFirmId);

        // THEN
        assertThat(result).isEmpty();

        verify(repository).findByIdAndFirmId(id, wrongFirmId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_managerProfile_WHEN_findById_THEN_returnsManagerRole() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();

        final UserProfileEntity entity = UserProfileEntity.builder()
                .id(id)
                .firmId(firmId)
                .email("manager@example.com")
                .role(Role.MANAGER)
                .build();

        when(repository.findByIdAndFirmId(id, firmId)).thenReturn(Optional.of(entity));

        // WHEN
        final Optional<UserProfile> result = adapter.findById(id, firmId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().role()).isEqualTo(Role.MANAGER);

        verify(repository).findByIdAndFirmId(id, firmId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_adminProfile_WHEN_findById_THEN_returnsAdminRole() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();

        final UserProfileEntity entity = UserProfileEntity.builder()
                .id(id)
                .firmId(firmId)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(repository.findByIdAndFirmId(id, firmId)).thenReturn(Optional.of(entity));

        // WHEN
        final Optional<UserProfile> result = adapter.findById(id, firmId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().role()).isEqualTo(Role.ADMIN);

        verify(repository).findByIdAndFirmId(id, firmId);
        verifyNoMoreInteractions(repository);
    }

    // ==================== findByFirmId() Tests ====================

    @Test
    void GIVEN_multipleProfiles_WHEN_findByFirmId_THEN_returnsAllProfiles() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID id3 = UUID.randomUUID();

        final List<UserProfileEntity> entities = List.of(
                UserProfileEntity.builder()
                        .id(id1)
                        .firmId(firmId)
                        .email("user1@example.com")
                        .role(Role.EMPLOYEE)
                        .build(),
                UserProfileEntity.builder()
                        .id(id2)
                        .firmId(firmId)
                        .email("user2@example.com")
                        .role(Role.MANAGER)
                        .build(),
                UserProfileEntity.builder()
                        .id(id3)
                        .firmId(firmId)
                        .email("user3@example.com")
                        .role(Role.ADMIN)
                        .build()
        );

        when(repository.findByFirmId(firmId)).thenReturn(entities);

        // WHEN
        final List<UserProfile> result = adapter.findByFirmId(firmId);

        // THEN
        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo(id1);
        assertThat(result.get(0).email()).isEqualTo("user1@example.com");
        assertThat(result.get(0).role()).isEqualTo(Role.EMPLOYEE);
        assertThat(result.get(1).id()).isEqualTo(id2);
        assertThat(result.get(1).email()).isEqualTo("user2@example.com");
        assertThat(result.get(1).role()).isEqualTo(Role.MANAGER);
        assertThat(result.get(2).id()).isEqualTo(id3);
        assertThat(result.get(2).email()).isEqualTo("user3@example.com");
        assertThat(result.get(2).role()).isEqualTo(Role.ADMIN);

        verify(repository).findByFirmId(firmId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_noProfiles_WHEN_findByFirmId_THEN_returnsEmptyList() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();

        when(repository.findByFirmId(firmId)).thenReturn(List.of());

        // WHEN
        final List<UserProfile> result = adapter.findByFirmId(firmId);

        // THEN
        assertThat(result).isEmpty();

        verify(repository).findByFirmId(firmId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_singleProfile_WHEN_findByFirmId_THEN_returnsSingleProfile() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final UUID id = UUID.randomUUID();

        final List<UserProfileEntity> entities = List.of(
                UserProfileEntity.builder()
                        .id(id)
                        .firmId(firmId)
                        .email("single@example.com")
                        .role(Role.EMPLOYEE)
                        .build()
        );

        when(repository.findByFirmId(firmId)).thenReturn(entities);

        // WHEN
        final List<UserProfile> result = adapter.findByFirmId(firmId);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(id);
        assertThat(result.get(0).email()).isEqualTo("single@example.com");

        verify(repository).findByFirmId(firmId);
        verifyNoMoreInteractions(repository);
    }

    // ==================== findByFirmIdAndRole() Tests ====================

    @Test
    void GIVEN_employeesExist_WHEN_findByFirmIdAndRole_THEN_returnsOnlyEmployees() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();

        final List<UserProfileEntity> entities = List.of(
                UserProfileEntity.builder()
                        .id(id1)
                        .firmId(firmId)
                        .email("employee1@example.com")
                        .role(Role.EMPLOYEE)
                        .build(),
                UserProfileEntity.builder()
                        .id(id2)
                        .firmId(firmId)
                        .email("employee2@example.com")
                        .role(Role.EMPLOYEE)
                        .build()
        );

        when(repository.findByFirmIdAndRole(firmId, Role.EMPLOYEE)).thenReturn(entities);

        // WHEN
        final List<UserProfile> result = adapter.findByFirmIdAndRole(firmId, Role.EMPLOYEE);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).role()).isEqualTo(Role.EMPLOYEE);
        assertThat(result.get(1).role()).isEqualTo(Role.EMPLOYEE);
        assertThat(result.get(0).email()).isEqualTo("employee1@example.com");
        assertThat(result.get(1).email()).isEqualTo("employee2@example.com");

        verify(repository).findByFirmIdAndRole(firmId, Role.EMPLOYEE);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_managersExist_WHEN_findByFirmIdAndRole_THEN_returnsOnlyManagers() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final UUID id = UUID.randomUUID();

        final List<UserProfileEntity> entities = List.of(
                UserProfileEntity.builder()
                        .id(id)
                        .firmId(firmId)
                        .email("manager@example.com")
                        .role(Role.MANAGER)
                        .build()
        );

        when(repository.findByFirmIdAndRole(firmId, Role.MANAGER)).thenReturn(entities);

        // WHEN
        final List<UserProfile> result = adapter.findByFirmIdAndRole(firmId, Role.MANAGER);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo(Role.MANAGER);
        assertThat(result.get(0).email()).isEqualTo("manager@example.com");

        verify(repository).findByFirmIdAndRole(firmId, Role.MANAGER);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_adminsExist_WHEN_findByFirmIdAndRole_THEN_returnsOnlyAdmins() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final UUID id = UUID.randomUUID();

        final List<UserProfileEntity> entities = List.of(
                UserProfileEntity.builder()
                        .id(id)
                        .firmId(firmId)
                        .email("admin@example.com")
                        .role(Role.ADMIN)
                        .build()
        );

        when(repository.findByFirmIdAndRole(firmId, Role.ADMIN)).thenReturn(entities);

        // WHEN
        final List<UserProfile> result = adapter.findByFirmIdAndRole(firmId, Role.ADMIN);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo(Role.ADMIN);
        assertThat(result.get(0).email()).isEqualTo("admin@example.com");

        verify(repository).findByFirmIdAndRole(firmId, Role.ADMIN);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_noProfilesWithRole_WHEN_findByFirmIdAndRole_THEN_returnsEmptyList() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();

        when(repository.findByFirmIdAndRole(firmId, Role.ADMIN)).thenReturn(List.of());

        // WHEN
        final List<UserProfile> result = adapter.findByFirmIdAndRole(firmId, Role.ADMIN);

        // THEN
        assertThat(result).isEmpty();

        verify(repository).findByFirmIdAndRole(firmId, Role.ADMIN);
        verifyNoMoreInteractions(repository);
    }

    // ==================== save() Tests ====================

    @Test
    void GIVEN_newProfile_WHEN_save_THEN_savesAndReturnsDomain() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();

        final UserProfile profile = UserProfile.builder()
                .id(id)
                .firmId(firmId)
                .email("newuser@example.com")
                .role(Role.EMPLOYEE)
                .build();

        final UserProfileEntity savedEntity = UserProfileEntity.builder()
                .id(id)
                .firmId(firmId)
                .email("newuser@example.com")
                .role(Role.EMPLOYEE)
                .build();

        when(repository.save(any(UserProfileEntity.class))).thenReturn(savedEntity);

        // WHEN
        final UserProfile result = adapter.save(profile);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.firmId()).isEqualTo(firmId);
        assertThat(result.email()).isEqualTo("newuser@example.com");
        assertThat(result.role()).isEqualTo(Role.EMPLOYEE);

        verify(repository).save(entityCaptor.capture());
        final UserProfileEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(id);
        assertThat(capturedEntity.getFirmId()).isEqualTo(firmId);
        assertThat(capturedEntity.getEmail()).isEqualTo("newuser@example.com");
        assertThat(capturedEntity.getRole()).isEqualTo(Role.EMPLOYEE);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_existingProfileUpdate_WHEN_save_THEN_updatesAndReturnsDomain() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();

        final UserProfile profile = UserProfile.builder()
                .id(id)
                .firmId(firmId)
                .email("updated@example.com")
                .role(Role.MANAGER)
                .build();

        final UserProfileEntity savedEntity = UserProfileEntity.builder()
                .id(id)
                .firmId(firmId)
                .email("updated@example.com")
                .role(Role.MANAGER)
                .build();

        when(repository.save(any(UserProfileEntity.class))).thenReturn(savedEntity);

        // WHEN
        final UserProfile result = adapter.save(profile);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.firmId()).isEqualTo(firmId);
        assertThat(result.email()).isEqualTo("updated@example.com");
        assertThat(result.role()).isEqualTo(Role.MANAGER);

        verify(repository).save(any(UserProfileEntity.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_profileWithAdminRole_WHEN_save_THEN_savesWithAdminRole() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();

        final UserProfile profile = UserProfile.builder()
                .id(id)
                .firmId(firmId)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        final UserProfileEntity savedEntity = UserProfileEntity.builder()
                .id(id)
                .firmId(firmId)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(repository.save(any(UserProfileEntity.class))).thenReturn(savedEntity);

        // WHEN
        final UserProfile result = adapter.save(profile);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(Role.ADMIN);

        verify(repository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_profileWithSpecialCharactersInEmail_WHEN_save_THEN_preservesEmail() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String specialEmail = "user+tag@sub.example.com";

        final UserProfile profile = UserProfile.builder()
                .id(id)
                .firmId(firmId)
                .email(specialEmail)
                .role(Role.EMPLOYEE)
                .build();

        final UserProfileEntity savedEntity = UserProfileEntity.builder()
                .id(id)
                .firmId(firmId)
                .email(specialEmail)
                .role(Role.EMPLOYEE)
                .build();

        when(repository.save(any(UserProfileEntity.class))).thenReturn(savedEntity);

        // WHEN
        final UserProfile result = adapter.save(profile);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(specialEmail);

        verify(repository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getEmail()).isEqualTo(specialEmail);
        verifyNoMoreInteractions(repository);
    }
}
