package com.aequitas.aequitascentralservice.app.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.aequitas.aequitascentralservice.app.port.outbound.ClockPort;
import com.aequitas.aequitascentralservice.app.port.outbound.CurrentUserPort;
import com.aequitas.aequitascentralservice.app.port.outbound.FirmRepositoryPort;
import com.aequitas.aequitascentralservice.domain.command.CreateFirmCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateFirmCommand;
import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import com.aequitas.aequitascentralservice.domain.value.Address;
import com.aequitas.aequitascentralservice.domain.value.CurrentUser;
import com.aequitas.aequitascentralservice.domain.value.Role;

/**
 * Unit tests for {@link FirmService} covering business logic and RBAC enforcement.
 */
@ExtendWith(MockitoExtension.class)
class FirmServiceTest {

    @Mock
    private FirmRepositoryPort repositoryPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private ClockPort clockPort;

    @InjectMocks
    private FirmService service;

    // ==================== getCurrentUserFirm() Tests ====================

    @Test
    void GIVEN_authenticatedUser_WHEN_getCurrentUserFirm_THEN_returnsFirm() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final CurrentUser currentUser = createCurrentUser(firmId, Role.EMPLOYEE);
        final Firm firm = createFirm(firmId);

        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(firmId)).thenReturn(Optional.of(firm));

        // WHEN
        final Firm result = service.getCurrentUserFirm();

        // THEN
        assertThat(result).isEqualTo(firm);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).findById(firmId);
        verifyNoMoreInteractions(currentUserPort, repositoryPort, clockPort);
    }

    @Test
    void GIVEN_firmNotFound_WHEN_getCurrentUserFirm_THEN_throwsException() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final CurrentUser currentUser = createCurrentUser(firmId, Role.EMPLOYEE);

        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(firmId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> service.getCurrentUserFirm())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Firm not found");
    }

    // ==================== findById() Tests ====================

    @Test
    void GIVEN_adminUser_WHEN_findById_THEN_returnsFirm() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final CurrentUser currentUser = createCurrentUser(UUID.randomUUID(), Role.ADMIN);
        final Firm firm = createFirm(firmId);

        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(firmId)).thenReturn(Optional.of(firm));

        // WHEN
        final Optional<Firm> result = service.findById(firmId);

        // THEN
        assertThat(result).isPresent().contains(firm);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).findById(firmId);
    }

    @Test
    void GIVEN_nonAdminUser_WHEN_findById_THEN_throwsAccessDeniedException() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final CurrentUser currentUser = createCurrentUser(UUID.randomUUID(), Role.EMPLOYEE);

        when(currentUserPort.currentUser()).thenReturn(currentUser);

        // WHEN & THEN
        assertThatThrownBy(() -> service.findById(firmId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only admins can perform this operation");
    }

    // ==================== list() Tests ====================

    @Test
    void GIVEN_adminUser_WHEN_list_THEN_returnsPagedFirms() {
        // GIVEN
        final CurrentUser currentUser = createCurrentUser(UUID.randomUUID(), Role.ADMIN);
        final PageRequest pageRequest = new PageRequest(20, null);
        final PageResult<Firm> pageResult = createPageResult();

        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.list(pageRequest)).thenReturn(pageResult);

        // WHEN
        final PageResult<Firm> result = service.list(pageRequest);

        // THEN
        assertThat(result).isEqualTo(pageResult);
        verify(currentUserPort).currentUser();
        verify(repositoryPort).list(pageRequest);
    }

    @Test
    void GIVEN_managerUser_WHEN_list_THEN_throwsAccessDeniedException() {
        // GIVEN
        final CurrentUser currentUser = createCurrentUser(UUID.randomUUID(), Role.MANAGER);
        final PageRequest pageRequest = new PageRequest(20, null);

        when(currentUserPort.currentUser()).thenReturn(currentUser);

        // WHEN & THEN
        assertThatThrownBy(() -> service.list(pageRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only admins can perform this operation");
    }

    // ==================== create() Tests ====================

    @Test
    void GIVEN_adminUserAndValidCommand_WHEN_create_THEN_createsFirm() {
        // GIVEN
        final CurrentUser currentUser = createCurrentUser(UUID.randomUUID(), Role.ADMIN);
        final CreateFirmCommand command = createCreateCommand();
        final Instant now = Instant.now();
        final Firm savedFirm = createFirm(UUID.randomUUID());

        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(clockPort.now()).thenReturn(now);
        when(repositoryPort.save(any(Firm.class))).thenReturn(savedFirm);

        // WHEN
        final UUID result = service.create(command);

        // THEN
        assertThat(result).isEqualTo(savedFirm.getId());
        verify(currentUserPort).currentUser();
        verify(clockPort).now();
        verify(repositoryPort).save(any(Firm.class));
    }

    @Test
    void GIVEN_employeeUser_WHEN_create_THEN_throwsAccessDeniedException() {
        // GIVEN
        final CurrentUser currentUser = createCurrentUser(UUID.randomUUID(), Role.EMPLOYEE);
        final CreateFirmCommand command = createCreateCommand();

        when(currentUserPort.currentUser()).thenReturn(currentUser);

        // WHEN & THEN
        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only admins can perform this operation");
    }

    // ==================== update() Tests ====================

    @Test
    void GIVEN_adminUserSameFirm_WHEN_update_THEN_updatesFirm() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final CurrentUser currentUser = createCurrentUser(firmId, Role.ADMIN);
        final Firm existingFirm = createFirm(firmId);
        final UpdateFirmCommand command = UpdateFirmCommand.builder()
                .name(Optional.of("Updated Name"))
                .address(Optional.empty())
                .build();
        final Instant now = Instant.now();

        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(firmId)).thenReturn(Optional.of(existingFirm));
        when(clockPort.now()).thenReturn(now);
        when(repositoryPort.save(any(Firm.class))).thenReturn(existingFirm);

        // WHEN
        service.update(firmId, command);

        // THEN
        verify(currentUserPort).currentUser();
        verify(repositoryPort).findById(firmId);
        verify(clockPort).now();
        verify(repositoryPort).save(any(Firm.class));
    }

    @Test
    void GIVEN_adminUserDifferentFirm_WHEN_update_THEN_throwsAccessDeniedException() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final UUID differentFirmId = UUID.randomUUID();
        final CurrentUser currentUser = createCurrentUser(differentFirmId, Role.ADMIN);
        final Firm existingFirm = createFirm(firmId);
        final UpdateFirmCommand command = UpdateFirmCommand.builder()
                .name(Optional.of("Updated Name"))
                .address(Optional.empty())
                .build();

        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(firmId)).thenReturn(Optional.of(existingFirm));

        // WHEN & THEN
        assertThatThrownBy(() -> service.update(firmId, command))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Cannot update a different firm");
    }

    @Test
    void GIVEN_nonAdminUser_WHEN_update_THEN_throwsAccessDeniedException() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final CurrentUser currentUser = createCurrentUser(firmId, Role.MANAGER);
        final UpdateFirmCommand command = createUpdateCommand();

        when(currentUserPort.currentUser()).thenReturn(currentUser);

        // WHEN & THEN
        assertThatThrownBy(() -> service.update(firmId, command))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only admins can perform this operation");
    }

    @Test
    void GIVEN_firmNotFound_WHEN_update_THEN_throwsIllegalArgumentException() {
        // GIVEN
        final UUID firmId = UUID.randomUUID();
        final CurrentUser currentUser = createCurrentUser(UUID.randomUUID(), Role.ADMIN);
        final UpdateFirmCommand command = createUpdateCommand();

        when(currentUserPort.currentUser()).thenReturn(currentUser);
        when(repositoryPort.findById(firmId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> service.update(firmId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Firm not found");
    }

    // ==================== Helper Methods ====================

    private CurrentUser createCurrentUser(final UUID firmId, final Role role) {
        return new CurrentUser(UUID.randomUUID(), firmId, role);
    }

    private Firm createFirm(final UUID firmId) {
        return Firm.builder()
                .id(firmId)
                .name("Test Firm")
                .address(createAddress())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Address createAddress() {
        return Address.builder()
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();
    }

    private CreateFirmCommand createCreateCommand() {
        return CreateFirmCommand.builder()
                .name("New Firm")
                .address(createAddress())
                .build();
    }

    private UpdateFirmCommand createUpdateCommand() {
        return UpdateFirmCommand.builder()
                .name(Optional.of("Updated Name"))
                .address(Optional.empty())
                .build();
    }

    private PageResult<Firm> createPageResult() {
        return new PageResult<>(
                java.util.List.of(createFirm(UUID.randomUUID())),
                null,
                1L,
                false);
    }
}
