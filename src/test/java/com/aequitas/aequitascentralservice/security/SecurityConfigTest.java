package com.aequitas.aequitascentralservice.security;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * Tests for {@link SecurityConfig}.
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

    @Test
    void GIVEN_jwtSecretProvided_WHEN_jwtDecoder_THEN_returnHmacJwtDecoder() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");

        // WHEN
        JwtDecoder decoder = config.jwtDecoder();

        // THEN
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
    }

    @Test
    void GIVEN_jwkSetUriProvided_WHEN_jwtDecoder_THEN_returnJwkSetJwtDecoder() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, TEST_JWK_SET_URI);

        // WHEN
        JwtDecoder decoder = config.jwtDecoder();

        // THEN
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
    }

    @Test
    void GIVEN_onlySupabaseJwkUri_WHEN_securityConfig_THEN_useSupabaseJwkUri() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);

        // WHEN
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");

        // THEN
        assertNotNull(config);
        JwtDecoder decoder = config.jwtDecoder();
        assertNotNull(decoder);
    }

    @Test
    void GIVEN_explicitJwkSetUri_WHEN_securityConfig_THEN_useExplicitJwkSetUri() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);

        // WHEN
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, TEST_JWK_SET_URI);

        // THEN
        assertNotNull(config);
        JwtDecoder decoder = config.jwtDecoder();
        assertNotNull(decoder);
    }

    @Test
    void GIVEN_jwtWithRoleClaim_WHEN_extractAuthorities_THEN_returnRoleAuthority() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", TEST_ROLE_ADMIN);
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void GIVEN_jwtWithLowercaseRoleClaim_WHEN_extractAuthorities_THEN_returnUppercaseRoleAuthority() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void GIVEN_jwtWithRoleInUserMetadata_WHEN_extractAuthorities_THEN_returnRoleAuthority() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> userMetadata = Map.of("role", TEST_ROLE_EMPLOYEE);
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", userMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    @Test
    void GIVEN_jwtWithRoleInAppMetadata_WHEN_extractAuthorities_THEN_returnRoleAuthority() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> appMetadata = Map.of("role", "manager");
        Map<String, Object> claims = new HashMap<>();
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
    }

    @Test
    void GIVEN_jwtWithNoRoleClaim_WHEN_extractAuthorities_THEN_returnEmptyAuthorities() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void GIVEN_jwtWithEmptyRoleClaim_WHEN_extractAuthorities_THEN_returnEmptyAuthorities() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "");
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void GIVEN_jwtWithWhitespaceRoleClaim_WHEN_extractAuthorities_THEN_returnEmptyAuthorities() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "   ");
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void GIVEN_jwtWithRoleNeedingTrim_WHEN_extractAuthorities_THEN_returnTrimmedRoleAuthority() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "  admin  ");
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void GIVEN_jwtWithRoleClaimPreferred_WHEN_extractAuthorities_THEN_ignoreMetadataRoles() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> userMetadata = Map.of("role", "employee");
        Map<String, Object> appMetadata = Map.of("role", "manager");
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", TEST_ROLE_ADMIN);
        claims.put("user_metadata", userMetadata);
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void GIVEN_jwtWithUserMetadataPreferred_WHEN_extractAuthorities_THEN_ignoreAppMetadata() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> userMetadata = Map.of("role", TEST_ROLE_EMPLOYEE);
        Map<String, Object> appMetadata = Map.of("role", "manager");
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", userMetadata);
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    @Test
    void GIVEN_jwtWithEmptyUserMetadata_WHEN_extractAuthorities_THEN_checkAppMetadata() {
        // GIVEN
        SupabaseProperties.Auth auth = new SupabaseProperties.Auth(SUPABASE_JWK_URI);
        SupabaseProperties properties = new SupabaseProperties("https://test.supabase.co", "key", auth);
        SecurityConfig config = new SecurityConfig(properties, TEST_JWT_SECRET, "");
        
        Map<String, Object> userMetadata = new HashMap<>();
        Map<String, Object> appMetadata = Map.of("role", "manager");
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_metadata", userMetadata);
        claims.put("app_metadata", appMetadata);
        claims.put("sub", TEST_JWT_SUBJECT);
        Jwt jwt = new Jwt("token", TEST_ISSUED_AT, TEST_EXPIRES_AT, 
                Map.of("alg", "HS256"), claims);
        
        JwtAuthenticationConverter converter = getJwtAuthenticationConverter(config);

        // WHEN
        Collection<GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // THEN
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
    }

    private JwtAuthenticationConverter getJwtAuthenticationConverter(SecurityConfig config) {
        try {
            java.lang.reflect.Method method = SecurityConfig.class.getDeclaredMethod("jwtAuthenticationConverter");
            method.setAccessible(true);
            return (JwtAuthenticationConverter) method.invoke(config);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Failed to get JwtAuthenticationConverter", e);
        }
    }
}

