package com.aequitas.aequitascentralservice.adapter.web.mapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.Role;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UserProfileResponse;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;

/**
 * Unit tests for {@link UserProfileDtoMapper} covering all mapping scenarios between domain models
 * and REST DTOs.
 *
 * <p>These tests verify that the static utility method correctly transforms domain user profile
 * aggregates to web-layer response objects, ensuring field mappings are accurate and complete.
 *
 * <p><strong>Testing Strategy:</strong> The public mapping method has dedicated tests covering:
 * <ul>
 *   <li>Standard mapping scenarios with all roles</li>
 *   <li>Field value preservation and correctness</li>
 *   <li>Role enum conversion between domain and DTO layers</li>
 * </ul>
 *
 * <p><strong>Note:</strong> Since {@code UserProfileDtoMapper} is annotated with {@code @UtilityClass},
 * all methods are static and no instance is created.
 */
class UserProfileDtoMapperTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FIRM_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String EMAIL = "user@example.com";

    @Test
    void GIVEN_userProfileWithEmployeeRole_WHEN_toResponse_THEN_returnsUserProfileResponseWithAllFieldsMapped() {
        // GIVEN
        final UserProfile profile =
                new UserProfile(USER_ID, UUID.randomUUID(), FIRM_ID, EMAIL, com.aequitas.aequitascentralservice.domain.value.Role.EMPLOYEE);

        // WHEN
        final UserProfileResponse response = UserProfileDtoMapper.toResponse(profile);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(USER_ID);
        assertThat(response.getFirmId()).isEqualTo(FIRM_ID);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getRole()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void GIVEN_userProfileWithManagerRole_WHEN_toResponse_THEN_returnsResponseWithManagerRole() {
        // GIVEN
        final UserProfile profile =
                new UserProfile(USER_ID, UUID.randomUUID(), FIRM_ID, EMAIL, com.aequitas.aequitascentralservice.domain.value.Role.MANAGER);

        // WHEN
        final UserProfileResponse response = UserProfileDtoMapper.toResponse(profile);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void GIVEN_userProfileWithAdminRole_WHEN_toResponse_THEN_returnsResponseWithAdminRole() {
        // GIVEN
        final UserProfile profile =
                new UserProfile(USER_ID, UUID.randomUUID(), FIRM_ID, EMAIL, com.aequitas.aequitascentralservice.domain.value.Role.ADMIN);

        // WHEN
        final UserProfileResponse response = UserProfileDtoMapper.toResponse(profile);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void GIVEN_userProfile_WHEN_toResponse_THEN_preservesAllIdentifiersCorrectly() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final String email = "test@test.com";
        final UserProfile profile =
                new UserProfile(userId, UUID.randomUUID(), firmId, email, com.aequitas.aequitascentralservice.domain.value.Role.EMPLOYEE);

        // WHEN
        final UserProfileResponse response = UserProfileDtoMapper.toResponse(profile);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getFirmId()).isEqualTo(firmId);
        assertThat(response.getEmail()).isEqualTo(email);
    }

    @Test
    void GIVEN_multipleProfilesWithDifferentRoles_WHEN_toResponse_THEN_correctlyMapsEachRole() {
        // GIVEN
        final UserProfile employee =
                new UserProfile(USER_ID, UUID.randomUUID(), FIRM_ID, "employee@example.com", com.aequitas.aequitascentralservice.domain.value.Role.EMPLOYEE);
        final UserProfile manager =
                new UserProfile(USER_ID, UUID.randomUUID(), FIRM_ID, "manager@example.com", com.aequitas.aequitascentralservice.domain.value.Role.MANAGER);
        final UserProfile admin =
                new UserProfile(USER_ID, UUID.randomUUID(), FIRM_ID, "admin@example.com", com.aequitas.aequitascentralservice.domain.value.Role.ADMIN);

        // WHEN
        final UserProfileResponse employeeResponse = UserProfileDtoMapper.toResponse(employee);
        final UserProfileResponse managerResponse = UserProfileDtoMapper.toResponse(manager);
        final UserProfileResponse adminResponse = UserProfileDtoMapper.toResponse(admin);

        // THEN
        assertThat(employeeResponse.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(managerResponse.getRole()).isEqualTo(Role.MANAGER);
        assertThat(adminResponse.getRole()).isEqualTo(Role.ADMIN);
    }
}
