package com.aequitas.aequitascentralservice.adapter.security;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;

@ExtendWith(MockitoExtension.class)
class JwtCurrentUserAdapterTest {

    @InjectMocks
    private JwtCurrentUserAdapter adapter;

    private SecurityContext securityContext;
    private Authentication authentication;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        jwt = mock(Jwt.class);
    }

    // ===== Happy Path Tests =====

    @Test
    void GIVEN_validJwtWithDirectClaims_WHEN_currentUser_THEN_returnsCurrentUser() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn("EMPLOYEE");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.firmId()).isEqualTo(firmId);
            assertThat(result.role()).isEqualTo(Role.EMPLOYEE);
        }
    }

    @Test
    void GIVEN_jwtWithRoleInUserMetadata_WHEN_currentUser_THEN_returnsCurrentUser() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("role", "MANAGER");

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(userMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.firmId()).isEqualTo(firmId);
            assertThat(result.role()).isEqualTo(Role.MANAGER);
        }
    }

    @Test
    void GIVEN_jwtWithRoleInAppMetadata_WHEN_currentUser_THEN_returnsCurrentUser() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("role", "ADMIN");

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.firmId()).isEqualTo(firmId);
            assertThat(result.role()).isEqualTo(Role.ADMIN);
        }
    }

    @Test
    void GIVEN_jwtWithFirmIdInUserMetadata_WHEN_currentUser_THEN_returnsCurrentUser() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("firm_id", firmId.toString());

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(userMetadata);
        when(jwt.getClaimAsString("role")).thenReturn("EMPLOYEE");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.firmId()).isEqualTo(firmId);
            assertThat(result.role()).isEqualTo(Role.EMPLOYEE);
        }
    }

    @Test
    void GIVEN_jwtWithFirmIdInAppMetadata_WHEN_currentUser_THEN_returnsCurrentUser() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("firm_id", firmId.toString());

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(jwt.getClaimAsString("role")).thenReturn("MANAGER");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.firmId()).isEqualTo(firmId);
            assertThat(result.role()).isEqualTo(Role.MANAGER);
        }
    }

    @Test
    void GIVEN_jwtWithRoleInLowercase_WHEN_currentUser_THEN_convertsToUppercase() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn("employee");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo(Role.EMPLOYEE);
        }
    }

    @Test
    void GIVEN_jwtWithRoleWithWhitespace_WHEN_currentUser_THEN_trimsAndConverts() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn("  admin  ");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo(Role.ADMIN);
        }
    }

    // ===== Exception Tests =====

    @Test
    void GIVEN_nullAuthentication_WHEN_currentUser_THEN_throwsIllegalStateException() {
        // GIVEN
        when(securityContext.getAuthentication()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing JWT authentication");
        }
    }

    @Test
    void GIVEN_nonJwtPrincipal_WHEN_currentUser_THEN_throwsIllegalStateException() {
        // GIVEN
        when(authentication.getPrincipal()).thenReturn("not-a-jwt");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing JWT authentication");
        }
    }

    @Test
    void GIVEN_missingFirmIdClaim_WHEN_currentUser_THEN_throwsIllegalStateException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(null);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required claim: firm_id");
        }
    }

    @Test
    void GIVEN_emptyFirmIdClaim_WHEN_currentUser_THEN_throwsIllegalStateException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn("");
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(null);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required claim: firm_id");
        }
    }

    @Test
    void GIVEN_blankFirmIdClaim_WHEN_currentUser_THEN_throwsIllegalStateException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn("   ");
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(null);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required claim: firm_id");
        }
    }

    @Test
    void GIVEN_missingRoleClaim_WHEN_currentUser_THEN_throwsIllegalStateException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(null);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required role claim");
        }
    }

    @Test
    void GIVEN_emptyRoleClaim_WHEN_currentUser_THEN_throwsIllegalStateException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn("");
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(null);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required role claim");
        }
    }

    @Test
    void GIVEN_userMetadataNotMap_WHEN_currentUser_THEN_skipsUserMetadata() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("firm_id", firmId.toString());

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn("not-a-map");
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(jwt.getClaimAsString("role")).thenReturn("EMPLOYEE");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.firmId()).isEqualTo(firmId);
            assertThat(result.role()).isEqualTo(Role.EMPLOYEE);
        }
    }

    @Test
    void GIVEN_appMetadataNotMap_WHEN_currentUser_THEN_skipsAppMetadata() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("role", "MANAGER");

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(userMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo(Role.MANAGER);
        }
    }

    @Test
    void GIVEN_emptyUserMetadataMap_WHEN_currentUser_THEN_checksAppMetadata() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> userMetadata = new HashMap<>();
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("firm_id", firmId.toString());

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(userMetadata);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(jwt.getClaimAsString("role")).thenReturn("ADMIN");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.firmId()).isEqualTo(firmId);
        }
    }

    @Test
    void GIVEN_invalidUuidInFirmId_WHEN_currentUser_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn("not-a-uuid");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void GIVEN_invalidRoleValue_WHEN_currentUser_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn("INVALID_ROLE");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void GIVEN_allThreeRoles_WHEN_currentUser_THEN_validateAllRoleEnums() {
        // GIVEN - EMPLOYEE
        final UUID userId1 = UUID.randomUUID();
        final UUID firmId1 = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId1.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId1.toString());
        when(jwt.getClaimAsString("role")).thenReturn("EMPLOYEE");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN - EMPLOYEE
            CurrentUser result = adapter.currentUser();

            // THEN - EMPLOYEE
            assertThat(result.role()).isEqualTo(Role.EMPLOYEE);

            // GIVEN - MANAGER
            when(jwt.getClaimAsString("role")).thenReturn("MANAGER");

            // WHEN - MANAGER
            result = adapter.currentUser();

            // THEN - MANAGER
            assertThat(result.role()).isEqualTo(Role.MANAGER);

            // GIVEN - ADMIN
            when(jwt.getClaimAsString("role")).thenReturn("ADMIN");

            // WHEN - ADMIN
            result = adapter.currentUser();

            // THEN - ADMIN
            assertThat(result.role()).isEqualTo(Role.ADMIN);
        }
    }

    @Test
    void GIVEN_userMetadataWithNullFirmId_WHEN_currentUser_THEN_checksAppMetadata() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("firm_id", null);
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("firm_id", firmId.toString());

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(userMetadata);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(jwt.getClaimAsString("role")).thenReturn("EMPLOYEE");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.firmId()).isEqualTo(firmId);
        }
    }

    @Test
    void GIVEN_userMetadataWithEmptyFirmId_WHEN_currentUser_THEN_checksAppMetadata() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("firm_id", "");
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("firm_id", firmId.toString());

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(userMetadata);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(jwt.getClaimAsString("role")).thenReturn("MANAGER");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.firmId()).isEqualTo(firmId);
        }
    }

    @Test
    void GIVEN_appMetadataWithNullFirmId_WHEN_currentUser_THEN_throwsException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("firm_id", null);

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required claim: firm_id");
        }
    }

    @Test
    void GIVEN_userMetadataWithNullRole_WHEN_currentUser_THEN_checksAppMetadata() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("role", null);
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("role", "ADMIN");

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(userMetadata);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo(Role.ADMIN);
        }
    }

    @Test
    void GIVEN_userMetadataWithEmptyRole_WHEN_currentUser_THEN_checksAppMetadata() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("role", "");
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("role", "EMPLOYEE");

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(userMetadata);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN
            final CurrentUser result = adapter.currentUser();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo(Role.EMPLOYEE);
        }
    }

    @Test
    void GIVEN_appMetadataWithNullRole_WHEN_currentUser_THEN_throwsException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("role", null);

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required role claim");
        }
    }

    @Test
    void GIVEN_appMetadataWithBlankFirmId_WHEN_currentUser_THEN_throwsException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("firm_id", "   ");

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required claim: firm_id");
        }
    }

    @Test
    void GIVEN_appMetadataWithBlankRole_WHEN_currentUser_THEN_throwsException() {
        // GIVEN
        final UUID userId = UUID.randomUUID();
        final UUID firmId = UUID.randomUUID();
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("role", "   ");

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("firm_id")).thenReturn(firmId.toString());
        when(jwt.getClaimAsString("role")).thenReturn(null);
        when(jwt.getClaim("user_metadata")).thenReturn(null);
        when(jwt.getClaim("app_metadata")).thenReturn(appMetadata);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // WHEN & THEN
            assertThatThrownBy(() -> adapter.currentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Missing required role claim");
        }
    }
}
