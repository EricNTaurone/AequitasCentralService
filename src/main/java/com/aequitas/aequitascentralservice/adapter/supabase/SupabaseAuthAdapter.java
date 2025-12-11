package com.aequitas.aequitascentralservice.adapter.supabase;

import java.util.Map;
import java.util.UUID;

import com.aequitas.aequitascentralservice.app.service.UserProfileService;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.aequitas.aequitascentralservice.app.port.outbound.SupabaseAuthPort;
import com.aequitas.aequitascentralservice.config.SupabaseProperties;
import com.aequitas.aequitascentralservice.constants.SupabaseConstants;
import com.aequitas.aequitascentralservice.domain.command.SignInCommand;
import com.aequitas.aequitascentralservice.domain.command.SignUpCommand;
import com.aequitas.aequitascentralservice.domain.model.SupabaseAuthSession;
import com.aequitas.aequitascentralservice.domain.model.SupabaseUser;
import com.aequitas.aequitascentralservice.domain.value.AuthTokens;
import com.aequitas.aequitascentralservice.domain.value.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HTTP adapter that calls Supabase Auth endpoints for user creation and
 * password authentication.
 */
@Component
public class SupabaseAuthAdapter implements SupabaseAuthPort {

    private final RestClient restClient;
    private final SupabaseProperties properties;
    private final UserProfileService userProfileService;

    public SupabaseAuthAdapter(final RestClient supabaseRestClient, final SupabaseProperties properties, final UserProfileService userProfileService) {
        if (!StringUtils.hasText(properties.serviceKey())) {
            throw new IllegalStateException("Supabase service key must be configured");
        }
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("Supabase base URL must be configured");
        }

        this.restClient = supabaseRestClient;
        this.properties = properties;
        this.userProfileService = userProfileService;
    }

    @Override
    public SupabaseUser createUser(final SignUpCommand command) {
        try {
            final SupabaseUserResponse response = restClient
                    .post()
                    .uri(String.format(SupabaseConstants.SIGNUP_URI, properties.url()))
                    .header(SupabaseConstants.API_KEY_HEADER, properties.serviceKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AdminCreateUserRequest(command.email(), command.password(), command.firmId(), command.role()))
                    .retrieve()
                    .body(SupabaseUserResponse.class);
            if (response == null) {
                throw new IllegalStateException("Supabase create user returned no payload");
            }
            final UserProfile profile = createUserProfile(response, command);
            return toDomainUser(response, profile);
        } catch (RestClientResponseException ex) {
            throw translate("create Supabase user", ex);
        }
    }

    @Override
    public SupabaseAuthSession signIn(final SignInCommand command) {
        try {
            final SupabaseSignInResponse response = restClient
                    .post()
                    .uri(String.format(SupabaseConstants.SIGNIN_URI, properties.url()))
                    .header(SupabaseConstants.API_KEY_HEADER, properties.serviceKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new SignInRequest(command.email(), command.password()))
                    .retrieve()
                    .body(SupabaseSignInResponse.class);
            if (response == null || response.user() == null) {
                throw new IllegalStateException("Supabase sign-in did not return a user");
            }
            final UserProfile profile = findUserProfile(UUID.fromString(response.user().id()));
            final SupabaseUser user = toDomainUser(response.user(), profile);
            final AuthTokens tokens
                    = new AuthTokens(response.accessToken(), response.refreshToken(), response.expiresIn(), response.tokenType());
            return new SupabaseAuthSession(user, tokens);
        } catch (RestClientResponseException ex) {
            throw translate("sign in with Supabase", ex);
        }
    }

    private UserProfile createUserProfile(final SupabaseUserResponse user, final SignUpCommand command) {
        return userProfileService.createUserProfile(UserProfile.builder()
                .email(user.email())
                .firmId(command.firmId())
                .role(command.role())
                .authenticationId(UUID.fromString(user.id()))
                .build());
    }

    private UserProfile findUserProfile(final UUID authenticationId) {
        return userProfileService.findByAuthenticationId(authenticationId);
    }

    private SupabaseUser toDomainUser(final SupabaseUserResponse response, final UserProfile profile) {
        final UUID authenticationId = UUID.fromString(response.id());
        return new SupabaseUser(authenticationId, profile.id(), profile.firmId(), response.email(), profile.role());
    }

    private IllegalStateException translate(final String action, final RestClientResponseException ex) {
        final HttpStatusCode status = ex.getStatusCode();
        final String message = ex.getResponseBodyAsString();
        return new IllegalStateException(
                "Failed to %s: %s %s".formatted(action, status, StringUtils.hasText(message) ? message : ""), ex);
    }

    private record AdminCreateUserRequest(
            String email,
            String password,
            @JsonProperty("email_confirm") boolean emailConfirm,
            @JsonProperty("user_metadata") Map<String, Object> userMetadata) {

        private AdminCreateUserRequest(final String email, final String password, final UUID firmId, final Role role) {
            this(email, password, true, Map.of("firm_id", firmId.toString(), "role", role.name()));
        }
    }

    private record SignInRequest(String email, String password) {

    }

    private record SupabaseUserResponse(
            String id,
            String email,
            @JsonProperty("app_metadata") Map<String, Object> appMetadata,
            @JsonProperty("user_metadata") Map<String, Object> userMetadata,
            String role,
            @JsonProperty("firm_id") String firmId) {

    }

    private record SupabaseSignInResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("token_type") String tokenType,
            SupabaseUserResponse user) {

    }
}
