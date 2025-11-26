package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.UserProfileEntity;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.Role;

/**
 * Tests for {@link UserProfileMapper} ensuring complete mapping between domain and entity.
 */
class UserProfileMapperTest {

    @Test
    void GIVEN_completeUserProfile_WHEN_toEntity_THEN_allFieldsMapped() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String email = "john.doe@example.com";
        final Role role = Role.ADMIN;

        final UserProfile profile = UserProfile.builder()
                .id(id)
                .firmId(firmId)
                .email(email)
                .role(role)
                .build();

        // WHEN
        final UserProfileEntity entity = UserProfileMapper.toEntity(profile);

        // THEN
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getFirmId()).isEqualTo(firmId);
        assertThat(entity.getEmail()).isEqualTo(email);
        assertThat(entity.getRole()).isEqualTo(role);
    }

    @Test
    void GIVEN_employeeRole_WHEN_toEntity_THEN_roleMappedCorrectly() {
        // GIVEN
        final UserProfile profile = UserProfile.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("employee@example.com")
                .role(Role.EMPLOYEE)
                .build();

        // WHEN
        final UserProfileEntity entity = UserProfileMapper.toEntity(profile);

        // THEN
        assertThat(entity.getRole()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void GIVEN_managerRole_WHEN_toEntity_THEN_roleMappedCorrectly() {
        // GIVEN
        final UserProfile profile = UserProfile.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("manager@example.com")
                .role(Role.MANAGER)
                .build();

        // WHEN
        final UserProfileEntity entity = UserProfileMapper.toEntity(profile);

        // THEN
        assertThat(entity.getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void GIVEN_adminRole_WHEN_toEntity_THEN_roleMappedCorrectly() {
        // GIVEN
        final UserProfile profile = UserProfile.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        // WHEN
        final UserProfileEntity entity = UserProfileMapper.toEntity(profile);

        // THEN
        assertThat(entity.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void GIVEN_emailWithSpecialCharacters_WHEN_toEntity_THEN_emailPreserved() {
        // GIVEN
        final String specialEmail = "user+tag@sub-domain.example.com";
        final UserProfile profile = UserProfile.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email(specialEmail)
                .role(Role.EMPLOYEE)
                .build();

        // WHEN
        final UserProfileEntity entity = UserProfileMapper.toEntity(profile);

        // THEN
        assertThat(entity.getEmail()).isEqualTo(specialEmail);
    }

    @Test
    void GIVEN_completeUserProfileEntity_WHEN_toDomain_THEN_allFieldsMapped() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String email = "jane.smith@example.com";
        final Role role = Role.MANAGER;

        final UserProfileEntity entity = UserProfileEntity.builder()
                .id(id)
                .firmId(firmId)
                .email(email)
                .role(role)
                .build();

        // WHEN
        final UserProfile domain = UserProfileMapper.toDomain(entity);

        // THEN
        assertThat(domain).isNotNull();
        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.firmId()).isEqualTo(firmId);
        assertThat(domain.email()).isEqualTo(email);
        assertThat(domain.role()).isEqualTo(role);
    }

    @Test
    void GIVEN_entityWithEmployeeRole_WHEN_toDomain_THEN_roleMappedCorrectly() {
        // GIVEN
        final UserProfileEntity entity = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("emp@example.com")
                .role(Role.EMPLOYEE)
                .build();

        // WHEN
        final UserProfile domain = UserProfileMapper.toDomain(entity);

        // THEN
        assertThat(domain.role()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void GIVEN_entityWithManagerRole_WHEN_toDomain_THEN_roleMappedCorrectly() {
        // GIVEN
        final UserProfileEntity entity = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("mgr@example.com")
                .role(Role.MANAGER)
                .build();

        // WHEN
        final UserProfile domain = UserProfileMapper.toDomain(entity);

        // THEN
        assertThat(domain.role()).isEqualTo(Role.MANAGER);
    }

    @Test
    void GIVEN_entityWithAdminRole_WHEN_toDomain_THEN_roleMappedCorrectly() {
        // GIVEN
        final UserProfileEntity entity = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        // WHEN
        final UserProfile domain = UserProfileMapper.toDomain(entity);

        // THEN
        assertThat(domain.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void GIVEN_entityWithSpecialEmailFormat_WHEN_toDomain_THEN_emailPreserved() {
        // GIVEN
        final String complexEmail = "test.user+label@corporate-domain.co.uk";
        final UserProfileEntity entity = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email(complexEmail)
                .role(Role.EMPLOYEE)
                .build();

        // WHEN
        final UserProfile domain = UserProfileMapper.toDomain(entity);

        // THEN
        assertThat(domain.email()).isEqualTo(complexEmail);
    }

    @Test
    void GIVEN_roundTripMapping_WHEN_toDomainAndToEntity_THEN_dataPreserved() {
        // GIVEN
        final UUID id = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String email = "roundtrip@example.com";
        final Role role = Role.MANAGER;

        final UserProfile original = UserProfile.builder()
                .id(id)
                .firmId(firmId)
                .email(email)
                .role(role)
                .build();

        // WHEN
        final UserProfileEntity entity = UserProfileMapper.toEntity(original);
        final UserProfile roundTrip = UserProfileMapper.toDomain(entity);

        // THEN
        assertThat(roundTrip).isEqualTo(original);
    }

    @Test
    void GIVEN_multipleProfiles_WHEN_toEntity_THEN_eachMappedIndependently() {
        // GIVEN
        final UserProfile profile1 = UserProfile.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("user1@example.com")
                .role(Role.EMPLOYEE)
                .build();

        final UserProfile profile2 = UserProfile.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("user2@example.com")
                .role(Role.ADMIN)
                .build();

        // WHEN
        final UserProfileEntity entity1 = UserProfileMapper.toEntity(profile1);
        final UserProfileEntity entity2 = UserProfileMapper.toEntity(profile2);

        // THEN
        assertThat(entity1.getId()).isNotEqualTo(entity2.getId());
        assertThat(entity1.getEmail()).isEqualTo("user1@example.com");
        assertThat(entity2.getEmail()).isEqualTo("user2@example.com");
        assertThat(entity1.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(entity2.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void GIVEN_multipleEntities_WHEN_toDomain_THEN_eachMappedIndependently() {
        // GIVEN
        final UserProfileEntity entity1 = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("entity1@example.com")
                .role(Role.MANAGER)
                .build();

        final UserProfileEntity entity2 = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .email("entity2@example.com")
                .role(Role.EMPLOYEE)
                .build();

        // WHEN
        final UserProfile domain1 = UserProfileMapper.toDomain(entity1);
        final UserProfile domain2 = UserProfileMapper.toDomain(entity2);

        // THEN
        assertThat(domain1.id()).isNotEqualTo(domain2.id());
        assertThat(domain1.email()).isEqualTo("entity1@example.com");
        assertThat(domain2.email()).isEqualTo("entity2@example.com");
        assertThat(domain1.role()).isEqualTo(Role.MANAGER);
        assertThat(domain2.role()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void GIVEN_sameFirmIdDifferentUsers_WHEN_toEntity_THEN_firmIdPreserved() {
        // GIVEN
        final UUID sharedFirmId = UUID.randomUUID();
        
        final UserProfile profile1 = UserProfile.builder()
                .id(UUID.randomUUID())
                .firmId(sharedFirmId)
                .email("user1@firm.com")
                .role(Role.EMPLOYEE)
                .build();

        final UserProfile profile2 = UserProfile.builder()
                .id(UUID.randomUUID())
                .firmId(sharedFirmId)
                .email("user2@firm.com")
                .role(Role.MANAGER)
                .build();

        // WHEN
        final UserProfileEntity entity1 = UserProfileMapper.toEntity(profile1);
        final UserProfileEntity entity2 = UserProfileMapper.toEntity(profile2);

        // THEN
        assertThat(entity1.getFirmId()).isEqualTo(sharedFirmId);
        assertThat(entity2.getFirmId()).isEqualTo(sharedFirmId);
        assertThat(entity1.getFirmId()).isEqualTo(entity2.getFirmId());
    }
}
