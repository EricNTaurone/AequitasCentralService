package com.aequitas.aequitascentralservice.security;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.aequitas.aequitascentralservice.config.SupabaseProperties;

/**
 * Production-grade JUnit 5 tests for {@link SecurityConfig}.
 * Tests JWT decoder configuration and authority extraction from JWT claims.
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    private static final String TEST_JWT_SECRET = "test-secret-key-for-hmac-256-must-be-long-enough";
    private static final String TEST_JWK_SET_URI = "https://example.supabase.co/.well-known/jwks.json";
    private static final String SUPABASE_JWK_URI = "https://supabase.example.com/.well-known/jwks.json";
    private static final String TEST_ROLE_ADMIN = "ADMIN";
    private static final String TEST_ROLE_EMPLOYEE = "EMPLOYEE";
    private static final String TEST_JWT_SUBJECT = "test-user-123";
    private static final Instant TEST_ISSUED_AT = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant TEST_EXPIRES_AT = Instant.parse("2024-01-01T01:00:00Z");

    private SupabaseProperties supabaseProperties;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        final SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        supabaseProperties = new SupabaseProperties("https://test.supabase.co", "test-key", auth);
    }

    @Test
    void GIVEN_emptyJwkSetUri_WHEN_jwtDecoder_THEN_returnHmacJwtDecoder() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");

        // WHEN
        final JwtDecoder result = securityConfig.jwtDecoder();

        // THEN
        assertThat(result).isNotNull().isInstanceOf(NimbusJwtDecoder.class);
    }

    @Test
    void GIVEN_explicitJwkSetUri_WHEN_jwtDecoder_THEN_returnJwkSetJwtDecoder() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, TEST_JWK_SET_URI);

        // WHEN
        final JwtDecoder result = securityConfig.jwtDecoder();

        // THEN
        assertThat(result).isNotNull().isInstanceOf(NimbusJwtDecoder.class);
    }

    @Test
    void GIVEN_nullJwkSetUri_WHEN_jwtDecoder_THEN_fallbackToSupabaseJwkUri() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, null);

        // WHEN
        final JwtDecoder result = securityConfig.jwtDecoder();

        // THEN
        assertThat(result).isNotNull().isInstanceOf(NimbusJwtDecoder.class);
    }

    @Test
    void GIVEN_jwtWithRoleClaimUppercase_WHEN_extractAuthorities_THEN_returnRoleAuthority() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", TEST_ROLE_ADMIN);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void GIVEN_jwtWithRoleClaimLowercase_WHEN_extractAuthorities_THEN_returnUppercaseRoleAuthority() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void GIVEN_jwtWithRoleClaimMixedCase_WHEN_extractAuthorities_THEN_returnUppercaseRoleAuthority() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", "AdMiN");
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void GIVEN_jwtWithRoleClaimWithWhitespace_WHEN_extractAuthorities_THEN_returnTrimmedRoleAuthority() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", "  admin  ");
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void GIVEN_jwtWithRoleInUserMetadata_WHEN_extractAuthorities_THEN_returnRoleAuthority() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> userMetadata = Map.of("role", TEST_ROLE_EMPLOYEE);
        final Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", userMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }

    @Test
    void GIVEN_jwtWithRoleInAppMetadata_WHEN_extractAuthorities_THEN_returnRoleAuthority() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> appMetadata = Map.of("role", "manager");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_MANAGER"));
    }

    @Test
    void GIVEN_jwtWithNoRoleClaim_WHEN_extractAuthorities_THEN_returnEmptyAuthorities() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void GIVEN_jwtWithEmptyRoleClaim_WHEN_extractAuthorities_THEN_returnEmptyAuthorities() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", "");
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void GIVEN_jwtWithWhitespaceOnlyRoleClaim_WHEN_extractAuthorities_THEN_returnEmptyAuthorities() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", "   ");
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void GIVEN_jwtWithRoleClaimAndMetadata_WHEN_extractAuthorities_THEN_preferTopLevelRoleClaim() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> userMetadata = Map.of("role", "employee");
        final Map<String, Object> appMetadata = Map.of("role", "manager");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", TEST_ROLE_ADMIN);
        claims.put("user_metadata", userMetadata);
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void GIVEN_jwtWithUserMetadataAndAppMetadata_WHEN_extractAuthorities_THEN_preferUserMetadata() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> userMetadata = Map.of("role", TEST_ROLE_EMPLOYEE);
        final Map<String, Object> appMetadata = Map.of("role", "manager");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", userMetadata);
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }

    @Test
    void GIVEN_jwtWithEmptyUserMetadataMap_WHEN_extractAuthorities_THEN_checkAppMetadata() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> userMetadata = new HashMap<>();
        final Map<String, Object> appMetadata = Map.of("role", "manager");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", userMetadata);
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_MANAGER"));
    }

    @Test
    void GIVEN_jwtWithNullRoleInUserMetadata_WHEN_extractAuthorities_THEN_checkAppMetadata() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("role", null);
        final Map<String, Object> appMetadata = Map.of("role", "supervisor");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", userMetadata);
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_SUPERVISOR"));
    }

    @Test
    void GIVEN_jwtWithEmptyStringRoleInUserMetadata_WHEN_extractAuthorities_THEN_checkAppMetadata() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> userMetadata = Map.of("role", "");
        final Map<String, Object> appMetadata = Map.of("role", "director");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", userMetadata);
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_DIRECTOR"));
    }

    @Test
    void GIVEN_jwtWithNonMapUserMetadata_WHEN_extractAuthorities_THEN_checkAppMetadata() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> appMetadata = Map.of("role", "analyst");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", "not-a-map");
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(new SimpleGrantedAuthority("ROLE_ANALYST"));
    }

    @Test
    void GIVEN_jwtWithNonMapAppMetadata_WHEN_extractAuthorities_THEN_returnEmpty() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> claims = new HashMap<>();
        claims.put("app_metadata", "not-a-map");
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void GIVEN_jwtWithEmptyAppMetadataMap_WHEN_extractAuthorities_THEN_returnEmpty() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> appMetadata = new HashMap<>();
        final Map<String, Object> claims = new HashMap<>();
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void GIVEN_jwtWithNullRoleInAppMetadata_WHEN_extractAuthorities_THEN_returnEmpty() {
        // GIVEN
        securityConfig = new SecurityConfig(supabaseProperties, TEST_JWT_SECRET, "");
        final Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("role", null);
        final Map<String, Object> claims = new HashMap<>();
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        final Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, Map.of("alg", "HS256"), claims);
        final JwtAuthenticationConverter converter = getJwtAuthenticationConverter(securityConfig);

        // WHEN
        final Collection<GrantedAuthority> result = filterRoleAuthorities(converter.convert(jwt).getAuthorities());

        // THEN
        assertThat(result).isNotNull().isEmpty();
    }

    @SuppressWarnings("unused")
    private JwtAuthenticationConverter getJwtAuthenticationConverter(final SecurityConfig config) {
        try {
            final java.lang.reflect.Method method =
                    SecurityConfig.class.getDeclaredMethod("jwtAuthenticationConverter");
            method.setAccessible(true);
            return (JwtAuthenticationConverter) method.invoke(config);
        } catch (final NoSuchMethodException
                | IllegalAccessException
                | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Failed to get JwtAuthenticationConverter", e);
        }
    }

    /**
     * Filters authorities to only include SimpleGrantedAuthority (role-based) authorities.
     * This excludes default Spring Security authorities like FactorGrantedAuthority.
     */
    private Collection<GrantedAuthority> filterRoleAuthorities(final Collection<GrantedAuthority> authorities) {
        return authorities.stream()
                .filter(authority -> authority instanceof SimpleGrantedAuthority)
                .toList();
    }
}

