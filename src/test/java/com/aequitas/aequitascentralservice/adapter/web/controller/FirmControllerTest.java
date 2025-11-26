package com.aequitas.aequitascentralservice.adapter.web.controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.CreateFirmRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.FirmPageResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.FirmResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.IdResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateFirmRequest;
import com.aequitas.aequitascentralservice.app.port.inbound.FirmCommandPort;
import com.aequitas.aequitascentralservice.app.port.inbound.FirmQueryPort;
import com.aequitas.aequitascentralservice.domain.command.CreateFirmCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateFirmCommand;
import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import com.aequitas.aequitascentralservice.domain.value.Address;

/**
 * Unit tests for {@link FirmController} covering endpoint behavior and DTO mapping.
 */
@ExtendWith(MockitoExtension.class)
class FirmControllerTest {

    private static final UUID FIRM_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final String FIRM_NAME = "Smith & Associates";

    @Mock
    private FirmQueryPort queryPort;

    @Mock
    private FirmCommandPort commandPort;

    @InjectMocks
    private FirmController controller;

    // ==================== getCurrentUserFirm() Tests ====================

    @Test
    void GIVEN_authenticatedUser_WHEN_getCurrentUserFirm_THEN_returnsFirm() {
        // GIVEN
        final Firm firm = createFirm(FIRM_ID);
        when(queryPort.getCurrentUserFirm()).thenReturn(firm);

        // WHEN
        final ResponseEntity<FirmResponse> response = controller.getCurrentUserFirm();

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(FIRM_ID, response.getBody().getId());
        assertEquals(FIRM_NAME, response.getBody().getName());
        verify(queryPort, times(1)).getCurrentUserFirm();
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    // ==================== getFirmById() Tests ====================

    @Test
    void GIVEN_existingFirm_WHEN_getFirmById_THEN_returnsFirm() {
        // GIVEN
        final Firm firm = createFirm(FIRM_ID);
        when(queryPort.findById(FIRM_ID)).thenReturn(Optional.of(firm));

        // WHEN
        final ResponseEntity<FirmResponse> response = controller.getFirmById(FIRM_ID);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(FIRM_ID, response.getBody().getId());
        assertEquals(FIRM_NAME, response.getBody().getName());
        verify(queryPort, times(1)).findById(FIRM_ID);
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    @Test
    void GIVEN_nonExistingFirm_WHEN_getFirmById_THEN_returns404() {
        // GIVEN
        when(queryPort.findById(FIRM_ID)).thenReturn(Optional.empty());

        // WHEN
        final ResponseEntity<FirmResponse> response = controller.getFirmById(FIRM_ID);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(queryPort, times(1)).findById(FIRM_ID);
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    // ==================== listFirms() Tests ====================

    @Test
    void GIVEN_defaultPagination_WHEN_listFirms_THEN_returnsPagedResults() {
        // GIVEN
        final Firm firm = createFirm(FIRM_ID);
        final PageResult<Firm> pageResult = new PageResult<>(
                List.of(firm),
                null,
                1L,
                false);
        when(queryPort.list(any(PageRequest.class))).thenReturn(pageResult);

        // WHEN
        final ResponseEntity<FirmPageResponse> response = controller.listFirms(20, null);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getItems().size());
        assertEquals(FIRM_ID, response.getBody().getItems().get(0).getId());
        assertEquals(1L, response.getBody().getTotal());
        assertEquals(false, response.getBody().getHasMore());
        verify(queryPort, times(1)).list(any(PageRequest.class));
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    @Test
    void GIVEN_customLimitAndCursor_WHEN_listFirms_THEN_passesCorrectPageRequest() {
        // GIVEN
        final String cursor = "some-cursor";
        final PageResult<Firm> pageResult = new PageResult<>(
                List.of(),
                null,
                0L,
                false);
        when(queryPort.list(any(PageRequest.class))).thenReturn(pageResult);

        // WHEN
        final ResponseEntity<FirmPageResponse> response = controller.listFirms(50, cursor);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(queryPort, times(1)).list(any(PageRequest.class));
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    // ==================== createFirm() Tests ====================

    @Test
    void GIVEN_validRequest_WHEN_createFirm_THEN_returns201WithId() {
        // GIVEN
        final CreateFirmRequest request = createCreateFirmRequest();
        final UUID newFirmId = UUID.randomUUID();
        when(commandPort.create(any(CreateFirmCommand.class))).thenReturn(newFirmId);

        // WHEN
        final ResponseEntity<IdResponse> response = controller.createFirm(request);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newFirmId, response.getBody().getId());
        verify(commandPort, times(1)).create(any(CreateFirmCommand.class));
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    // ==================== updateFirm() Tests ====================

    @Test
    void GIVEN_validRequest_WHEN_updateFirm_THEN_returns204() {
        // GIVEN
        final UpdateFirmRequest request = createUpdateFirmRequest();

        // WHEN
        final ResponseEntity<Void> response = controller.updateFirm(FIRM_ID, request);

        // THEN
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandPort, times(1)).update(eq(FIRM_ID), any(UpdateFirmCommand.class));
        verifyNoMoreInteractions(queryPort, commandPort);
    }

    // ==================== Helper Methods ====================

    private Firm createFirm(final UUID id) {
        return Firm.builder()
                .id(id)
                .name(FIRM_NAME)
                .address(createAddress())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Address createAddress() {
        return Address.builder()
                .street("123 Main Street")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();
    }

    private CreateFirmRequest createCreateFirmRequest() {
        final CreateFirmRequest request = new CreateFirmRequest();
        request.setName("New Firm");
        
        final com.aequitas.aequitascentralservice.adapter.web.generated.dto.Address address =
                new com.aequitas.aequitascentralservice.adapter.web.generated.dto.Address();
        address.setStreet("123 Main Street");
        address.setCity("New York");
        address.setState("NY");
        address.setPostalCode("10001");
        address.setCountry("USA");
        request.setAddress(address);
        
        return request;
    }

    private UpdateFirmRequest createUpdateFirmRequest() {
        final UpdateFirmRequest request = new UpdateFirmRequest();
        request.setName("Updated Firm Name");
        return request;
    }
}
