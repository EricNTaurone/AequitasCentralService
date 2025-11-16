package com.aequitas.aequitascentralservice.adapter.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateUserRoleRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UserProfileResponse;
import com.aequitas.aequitascentralservice.app.port.inbound.UserProfileCommandPort;
import com.aequitas.aequitascentralservice.app.port.inbound.UserProfileQueryPort;
import com.aequitas.aequitascentralservice.domain.model.UserProfile;
import com.aequitas.aequitascentralservice.domain.value.Role;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FIRM_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String EMAIL = "user@example.com";

    @Mock
    private UserProfileQueryPort queryPort;

    @Mock
    private UserProfileCommandPort commandPort;

    @Captor
    private ArgumentCaptor<Optional<Role>> roleCaptor;

    @InjectMocks
    private UserProfileController controller;

    @Test
    void GIVEN_authenticatedUser_WHEN_me_THEN_returnsOwnProfile() {
        // GIVEN
        UserProfile profile = profile(Role.MANAGER);
        when(queryPort.me()).thenReturn(profile);

        // WHEN
        ResponseEntity<UserProfileResponse> response = controller.me();

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_ID, response.getBody().getId());
        assertEquals(FIRM_ID, response.getBody().getFirmId());
        assertEquals(EMAIL, response.getBody().getEmail());
        assertEquals(com.aequitas.aequitascentralservice.adapter.web.generated.dto.Role.MANAGER,
                response.getBody().getRole());
        verify(queryPort, times(1)).me();
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    @Test
    void GIVEN_roleFilter_WHEN_list_THEN_optionalPassedToQueryPort() {
        // GIVEN
        UserProfile profile = profile(Role.MANAGER);
        when(queryPort.list(Optional.of(Role.MANAGER))).thenReturn(List.of(profile));

        // WHEN
        ResponseEntity<List<UserProfileResponse>> response = controller.list("manager");

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(USER_ID, response.getBody().get(0).getId());
        assertEquals(FIRM_ID, response.getBody().get(0).getFirmId());
        assertEquals(EMAIL, response.getBody().get(0).getEmail());
        assertEquals(com.aequitas.aequitascentralservice.adapter.web.generated.dto.Role.MANAGER,
                response.getBody().get(0).getRole());
        verify(queryPort, times(1)).list(roleCaptor.capture());
        assertEquals(Optional.of(Role.MANAGER), roleCaptor.getValue());
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    @Test
    void GIVEN_invalidRole_WHEN_list_THEN_throwsIllegalArgument() {
        // GIVEN
        // WHEN
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> controller.list("invalid-role"));

        // THEN
        assertNotNull(exception);
        assertEquals("No enum constant com.aequitas.aequitascentralservice.domain.value.Role.INVALID-ROLE",
                exception.getMessage());
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    @Test
    void GIVEN_adminRequest_WHEN_updateRole_THEN_commandPortInvoked() {
        // GIVEN
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(com.aequitas.aequitascentralservice.adapter.web.generated.dto.Role.ADMIN);

        // WHEN
        ResponseEntity<Void> response = controller.updateRole(USER_ID, request);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandPort, times(1)).updateRole(USER_ID, Role.ADMIN);
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    private UserProfile profile(final Role role) {
        return new UserProfile(USER_ID, FIRM_ID, EMAIL, role);
    }
}
