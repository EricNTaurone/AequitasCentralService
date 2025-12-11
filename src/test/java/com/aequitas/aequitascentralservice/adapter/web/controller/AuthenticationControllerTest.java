package com.aequitas.aequitascentralservice.adapter.web.controller;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.AuthResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.Role;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.SignInRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.SignUpRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UserProfileResponse;
import com.aequitas.aequitascentralservice.app.port.inbound.AuthenticationCommandPort;
import com.aequitas.aequitascentralservice.domain.model.AuthSession;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.AuthTokens;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private static final UUID FIRM_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final String EMAIL = "user@example.com";

    @Mock
    private AuthenticationCommandPort authenticationCommandPort;

    @InjectMocks
    private AuthenticationController controller;

    @Test
    void signUp_returnsCreatedResponse() {
        // GIVEN
        SignUpRequest request = new SignUpRequest(FIRM_ID, EMAIL, "password123", Role.ADMIN);

        AuthSession session = new AuthSession(
                new UserProfile(
                        USER_ID, UUID.randomUUID(), FIRM_ID, EMAIL,
                        com.aequitas.aequitascentralservice.domain.value.Role.ADMIN),
                new AuthTokens("access", "refresh", 3600, "bearer"));
        when(authenticationCommandPort.signUp(any())).thenReturn(session);

        // WHEN
        ResponseEntity<AuthResponse> response = controller.signUp(request);

        // THEN
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(USER_ID, response.getBody().getUser().getId());
        assertEquals("access", response.getBody().getTokens().getAccessToken());
        verify(authenticationCommandPort, times(1)).signUp(any());
    }

    @Test
    void signIn_returnsOkResponse() {
        // GIVEN
        SignInRequest request = new SignInRequest(EMAIL, "password123");

        AuthSession session = new AuthSession(
                new UserProfile(
                        USER_ID, UUID.randomUUID(), FIRM_ID, EMAIL,
                        com.aequitas.aequitascentralservice.domain.value.Role.MANAGER),
                new AuthTokens("access2", "refresh2", 1800, "bearer"));
        when(authenticationCommandPort.signIn(any())).thenReturn(session);

        // WHEN
        ResponseEntity<AuthResponse> response = controller.signIn(request);

        // THEN
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserProfileResponse user = response.getBody().getUser();
        assertEquals(
                com.aequitas.aequitascentralservice.domain.value.Role.MANAGER.name(),
                user.getRole().name());
        assertEquals("access2", response.getBody().getTokens().getAccessToken());
        verify(authenticationCommandPort, times(1)).signIn(any());
    }
}
