package com.aequitas.aequitascentralservice.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

import com.aequitas.aequitascentralservice.config.SupabaseProperties;

/**
 * Configures JWT-based authentication and locking down all API endpoints by default.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String jwtSecret;
    private final String jwkSetUri;

    public SecurityConfig(
            final SupabaseProperties supabaseProperties,
            @Value("${security.jwt.secret:dev-secret-key-please-override}") final String jwtSecret,
            @Value("${security.jwt.jwk-set-uri:}") final String jwkSetUri) {
        this.jwtSecret = jwtSecret;
        final String supabaseJwk = supabaseProperties.auth().jwkSetUri();
        this.jwkSetUri = StringUtils.hasText(jwkSetUri) ? jwkSetUri : supabaseJwk;
    }

    /**
     * Configures the stateless security filter chain.
     *
     * @param http http builder.
     * @return configured filter chain.
     * @throws Exception when configuration fails.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        requests ->
                                requests.requestMatchers("/actuator/health", "/actuator/info")
                                        .permitAll()
                                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                                        .permitAll()
                                        .requestMatchers("/api/v1/auth/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/actuator/**")
                                        .authenticated()
                                        .requestMatchers("/api/**")
                                        .authenticated()
                                        .anyRequest()
                                        .authenticated())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Configures the decoder for HMAC signed JWTs.
     *
     * @return configured decoder.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        if (StringUtils.hasText(jwkSetUri)) {
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
        final byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(keyBytes, "HmacSHA256")).build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private java.util.Collection<GrantedAuthority> extractAuthorities(final Jwt jwt) {
        final String role = resolveRole(jwt);
        if (role == null) {
            return java.util.List.of();
        }
        return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private String resolveRole(final Jwt jwt) {
        final String role = jwt.getClaimAsString("role");
        if (StringUtils.hasText(role)) {
            return role.trim().toUpperCase();
        }
        final Object userMetadata = jwt.getClaim("user_metadata");
        if (userMetadata instanceof java.util.Map<?, ?> metadata) {
            final Object nested = metadata.get("role");
            if (nested != null && StringUtils.hasText(String.valueOf(nested))) {
                return String.valueOf(nested).trim().toUpperCase();
            }
        }
        final Object appMetadata = jwt.getClaim("app_metadata");
        if (appMetadata instanceof java.util.Map<?, ?> metadata) {
            final Object nested = metadata.get("role");
            if (nested != null && StringUtils.hasText(String.valueOf(nested))) {
                return String.valueOf(nested).trim().toUpperCase();
            }
        }
        return null;
    }
}
