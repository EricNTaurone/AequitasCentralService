package com.aequitas.aequitascentralservice.adapter.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.aequitas.aequitascentralservice.adapter.persistence.embeddable.AddressEmbeddable;
import com.aequitas.aequitascentralservice.adapter.persistence.entity.FirmEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.mapper.FirmMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.FirmJpaRepository;
import com.aequitas.aequitascentralservice.domain.model.Firm;
import com.aequitas.aequitascentralservice.domain.pagination.PageRequest;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import com.aequitas.aequitascentralservice.domain.value.Address;

/**
 * Unit tests for {@link FirmRepositoryAdapter} covering persistence operations.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FirmRepositoryAdapterTest {

    @Mock
    private FirmJpaRepository repository;

    @InjectMocks
    private FirmRepositoryAdapter adapter;

    // ==================== save() Tests ====================
    @Test
    void GIVEN_validFirm_WHEN_save_THEN_savesAndReturnsDomain() {
        try (final MockedStatic<FirmMapper> mapperMock = mockStatic(FirmMapper.class)) {
            // GIVEN
            final UUID id = UUID.randomUUID();
            final Firm domainFirm = createFirm(id);
            final FirmEntity entityToSave = createFirmEntity(id);
            final FirmEntity savedEntity = createFirmEntity(id);
            final Firm returnedDomain = createFirm(id);

            mapperMock.when(() -> FirmMapper.toEntity(domainFirm)).thenReturn(entityToSave);
            mapperMock.when(() -> FirmMapper.toDomain(savedEntity)).thenReturn(returnedDomain);
            when(repository.save(entityToSave)).thenReturn(savedEntity);

            // WHEN
            final Firm result = adapter.save(domainFirm);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);

            mapperMock.verify(() -> FirmMapper.toEntity(domainFirm));
            mapperMock.verify(() -> FirmMapper.toDomain(savedEntity));
            verify(repository).save(entityToSave);
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_firmWithAllFields_WHEN_save_THEN_preservesAllFields() {
        try (final MockedStatic<FirmMapper> mapperMock = mockStatic(FirmMapper.class)) {
            // GIVEN
            final UUID id = UUID.randomUUID();
            final Firm domainFirm = createFirm(id);
            final FirmEntity entityToSave = createFirmEntity(id);
            final FirmEntity savedEntity = createFirmEntity(id);
            final Firm returnedDomain = createFirm(id);

            mapperMock.when(() -> FirmMapper.toEntity(domainFirm)).thenReturn(entityToSave);
            mapperMock.when(() -> FirmMapper.toDomain(savedEntity)).thenReturn(returnedDomain);
            when(repository.save(entityToSave)).thenReturn(savedEntity);

            // WHEN
            final Firm result = adapter.save(domainFirm);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(domainFirm.getName());
            assertThat(result.getAddress()).isEqualTo(domainFirm.getAddress());

            mapperMock.verify(() -> FirmMapper.toEntity(domainFirm));
            mapperMock.verify(() -> FirmMapper.toDomain(savedEntity));
            verify(repository).save(entityToSave);
            verifyNoMoreInteractions(repository);
        }
    }

    // ==================== findById() Tests ====================
    @Test
    void GIVEN_existingFirm_WHEN_findById_THEN_returnsFirm() {
        try (final MockedStatic<FirmMapper> mapperMock = mockStatic(FirmMapper.class)) {
            // GIVEN
            final UUID id = UUID.randomUUID();
            final FirmEntity entity = createFirmEntity(id);
            final Firm domainFirm = createFirm(id);

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            mapperMock.when(() -> FirmMapper.toDomain(entity)).thenReturn(domainFirm);

            // WHEN
            final Optional<Firm> result = adapter.findById(id);

            // THEN
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);

            verify(repository).findById(id);
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_nonExistingFirm_WHEN_findById_THEN_returnsEmpty() {
        // GIVEN
        final UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        // WHEN
        final Optional<Firm> result = adapter.findById(id);

        // THEN
        assertThat(result).isEmpty();

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
    }

    // ==================== list() Tests ====================
    @Test
    void GIVEN_emptyDatabase_WHEN_list_THEN_returnsEmptyPage() {
        // GIVEN
        final PageRequest pageRequest = new PageRequest(20, null);
        final List<FirmEntity> entities = List.of();
        final Page<FirmEntity> page = new PageImpl<>(entities);

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // WHEN
        final PageResult<Firm> result = adapter.list(pageRequest);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.items()).isEmpty();
        assertThat(result.hasMore()).isFalse();
        assertThat(result.nextCursor()).isNull();

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void GIVEN_multipleFirms_WHEN_list_THEN_returnsPagedResults() {
        try (final MockedStatic<FirmMapper> mapperMock = mockStatic(FirmMapper.class)) {
            // GIVEN
            final PageRequest pageRequest = new PageRequest(20, null);
            final UUID id1 = UUID.randomUUID();
            final UUID id2 = UUID.randomUUID();
            final FirmEntity entity1 = createFirmEntity(id1);
            final FirmEntity entity2 = createFirmEntity(id2);
            final List<FirmEntity> entities = List.of(entity1, entity2);
            final Page<FirmEntity> page = new PageImpl<>(entities);

            final Firm domain1 = createFirm(id1);
            final Firm domain2 = createFirm(id2);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> FirmMapper.toDomain(entity1)).thenReturn(domain1);
            mapperMock.when(() -> FirmMapper.toDomain(entity2)).thenReturn(domain2);

            // WHEN
            final PageResult<Firm> result = adapter.list(pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(2);
            assertThat(result.items().get(0).getId()).isEqualTo(id1);
            assertThat(result.items().get(1).getId()).isEqualTo(id2);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_fullPage_WHEN_list_THEN_hasMoreIsTrue() {
        try (final MockedStatic<FirmMapper> mapperMock = mockStatic(FirmMapper.class)) {
            // GIVEN
            final PageRequest pageRequest = new PageRequest(2, null);
            final UUID id1 = UUID.randomUUID();
            final UUID id2 = UUID.randomUUID();
            final FirmEntity entity1 = createFirmEntity(id1);
            final FirmEntity entity2 = createFirmEntity(id2);
            final List<FirmEntity> entities = List.of(entity1, entity2);
            final Page<FirmEntity> page = new PageImpl<>(entities);

            final Firm domain1 = createFirm(id1);
            final Firm domain2 = createFirm(id2);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> FirmMapper.toDomain(entity1)).thenReturn(domain1);
            mapperMock.when(() -> FirmMapper.toDomain(entity2)).thenReturn(domain2);

            // WHEN
            final PageResult<Firm> result = adapter.list(pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(2);
            assertThat(result.hasMore()).isTrue();
            assertThat(result.nextCursor()).isNotNull();

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_validCursor_WHEN_list_THEN_paginatesCorrectly() {
        try (final MockedStatic<FirmMapper> mapperMock = mockStatic(FirmMapper.class)) {
            // GIVEN
            final UUID cursorId = UUID.randomUUID();
            final PageRequest pageRequest = new PageRequest(10, cursorId.toString());

            final UUID id = UUID.randomUUID();
            final FirmEntity entity = createFirmEntity(id);
            final List<FirmEntity> entities = List.of(entity);
            final Page<FirmEntity> page = new PageImpl<>(entities);
            final Firm domain = createFirm(id);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> FirmMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<Firm> result = adapter.list(pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    // ==================== Helper Methods ====================
    private Firm createFirm(final UUID id) {
        return Firm.builder()
                .id(id)
                .name("Test Firm")
                .address(createAddress())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private FirmEntity createFirmEntity(final UUID id) {
        final AddressEmbeddable address = AddressEmbeddable.builder()
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();
        return FirmEntity.builder()
                .id(id)
                .name("Test Firm")
                .address(address)
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
}
