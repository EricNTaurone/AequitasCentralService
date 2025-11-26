package com.aequitas.aequitascentralservice.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.TimeEntryEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.mapper.TimeEntryMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.TimeEntryJpaRepository;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import com.aequitas.aequitascentralservice.domain.model.TimeEntryFilter;
import com.aequitas.aequitascentralservice.domain.pagination.PageResult;
import com.aequitas.aequitascentralservice.domain.value.EntryStatus;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class TimeEntryRepositoryAdapterTest {

    @Mock
    private TimeEntryJpaRepository repository;

    @InjectMocks
    private TimeEntryRepositoryAdapter adapter;

    // ==================== save() Tests ====================

    @Test
    void GIVEN_validTimeEntry_WHEN_save_THEN_savesAndReturnsDomain() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID id = UUID.randomUUID();
            final UUID firmId = UUID.randomUUID();
            final TimeEntry domainEntry = createTimeEntry(id, firmId, EntryStatus.DRAFT);
            final TimeEntryEntity entityToSave = createTimeEntryEntity(id, firmId, EntryStatus.DRAFT);
            final TimeEntryEntity savedEntity = createTimeEntryEntity(id, firmId, EntryStatus.DRAFT);
            final TimeEntry returnedDomain = createTimeEntry(id, firmId, EntryStatus.DRAFT);

            mapperMock.when(() -> TimeEntryMapper.toEntity(domainEntry)).thenReturn(entityToSave);
            mapperMock.when(() -> TimeEntryMapper.toDomain(savedEntity)).thenReturn(returnedDomain);
            when(repository.save(entityToSave)).thenReturn(savedEntity);

            // WHEN
            final TimeEntry result = adapter.save(domainEntry);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getFirmId()).isEqualTo(firmId);
            assertThat(result.getStatus()).isEqualTo(EntryStatus.DRAFT);

            mapperMock.verify(() -> TimeEntryMapper.toEntity(domainEntry));
            mapperMock.verify(() -> TimeEntryMapper.toDomain(savedEntity));
            verify(repository).save(entityToSave);
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_timeEntryWithAllFields_WHEN_save_THEN_preservesAllFields() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID id = UUID.randomUUID();
            final UUID firmId = UUID.randomUUID();
            final UUID matterId = UUID.randomUUID();
            final UUID approvedBy = UUID.randomUUID();
            final Instant approvedAt = Instant.now();

            final TimeEntry domainEntry = createCompleteTimeEntry(id, firmId, matterId, approvedBy, approvedAt);
            final TimeEntryEntity entityToSave = createCompleteTimeEntryEntity(id, firmId, matterId, approvedBy, approvedAt);
            final TimeEntryEntity savedEntity = createCompleteTimeEntryEntity(id, firmId, matterId, approvedBy, approvedAt);
            final TimeEntry returnedDomain = createCompleteTimeEntry(id, firmId, matterId, approvedBy, approvedAt);

            mapperMock.when(() -> TimeEntryMapper.toEntity(domainEntry)).thenReturn(entityToSave);
            mapperMock.when(() -> TimeEntryMapper.toDomain(savedEntity)).thenReturn(returnedDomain);
            when(repository.save(entityToSave)).thenReturn(savedEntity);

            // WHEN
            final TimeEntry result = adapter.save(domainEntry);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getMatterId()).isEqualTo(matterId);
            assertThat(result.getApprovedBy()).isEqualTo(approvedBy);
            assertThat(result.getApprovedAt()).isEqualTo(approvedAt);
            assertThat(result.getStatus()).isEqualTo(EntryStatus.APPROVED);

            mapperMock.verify(() -> TimeEntryMapper.toEntity(domainEntry));
            mapperMock.verify(() -> TimeEntryMapper.toDomain(savedEntity));
            verify(repository).save(entityToSave);
            verifyNoMoreInteractions(repository);
        }
    }

    // ==================== findById() Tests ====================

    @Test
    void GIVEN_existingEntry_WHEN_findById_THEN_returnsEntry() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID id = UUID.randomUUID();
            final UUID firmId = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntity(id, firmId, EntryStatus.SUBMITTED);
            final TimeEntry domainEntry = createTimeEntry(id, firmId, EntryStatus.SUBMITTED);

            when(repository.findByIdAndFirmId(id, firmId)).thenReturn(Optional.of(entity));
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domainEntry);

            // WHEN
            final Optional<TimeEntry> result = adapter.findById(id, firmId);

            // THEN
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
            assertThat(result.get().getFirmId()).isEqualTo(firmId);
            assertThat(result.get().getStatus()).isEqualTo(EntryStatus.SUBMITTED);

            verify(repository).findByIdAndFirmId(id, firmId);
            mapperMock.verify(() -> TimeEntryMapper.toDomain(entity));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_nonExistingEntry_WHEN_findById_THEN_returnsEmpty() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID id = UUID.randomUUID();
            final UUID firmId = UUID.randomUUID();

            when(repository.findByIdAndFirmId(id, firmId)).thenReturn(Optional.empty());

            // WHEN
            final Optional<TimeEntry> result = adapter.findById(id, firmId);

            // THEN
            assertThat(result).isEmpty();

            verify(repository).findByIdAndFirmId(id, firmId);
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }

    @Test
    void GIVEN_entryFromDifferentFirm_WHEN_findById_THEN_returnsEmpty() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID id = UUID.randomUUID();
            final UUID wrongFirmId = UUID.randomUUID();

            when(repository.findByIdAndFirmId(id, wrongFirmId)).thenReturn(Optional.empty());

            // WHEN
            final Optional<TimeEntry> result = adapter.findById(id, wrongFirmId);

            // THEN
            assertThat(result).isEmpty();

            verify(repository).findByIdAndFirmId(id, wrongFirmId);
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }

    // ==================== search() Tests ====================

    @Test
    void GIVEN_emptyFilter_WHEN_search_THEN_returnsAllForFirm() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final UUID id1 = UUID.randomUUID();
            final UUID id2 = UUID.randomUUID();
            final TimeEntryEntity entity1 = createTimeEntryEntity(id1, firmId, EntryStatus.DRAFT);
            final TimeEntryEntity entity2 = createTimeEntryEntity(id2, firmId, EntryStatus.SUBMITTED);
            final List<TimeEntryEntity> entities = List.of(entity1, entity2);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);

            final TimeEntry domain1 = createTimeEntry(id1, firmId, EntryStatus.DRAFT);
            final TimeEntry domain2 = createTimeEntry(id2, firmId, EntryStatus.SUBMITTED);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity1)).thenReturn(domain1);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity2)).thenReturn(domain2);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(2);
            assertThat(result.items().get(0).getId()).isEqualTo(id1);
            assertThat(result.items().get(1).getId()).isEqualTo(id2);
            assertThat(result.totalItems()).isEqualTo(2);
            assertThat(result.hasMore()).isFalse();
            assertThat(result.nextCursor()).isNull();

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_filterWithCustomerId_WHEN_search_THEN_filtersCorrectly() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final UUID customerId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.of(customerId), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntityWithCustomer(id, firmId, customerId, EntryStatus.DRAFT);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntryWithCustomer(id, firmId, customerId, EntryStatus.DRAFT);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).getCustomerId()).isEqualTo(customerId);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_filterWithProjectId_WHEN_search_THEN_filtersCorrectly() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.of(projectId), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntityWithProject(id, firmId, projectId, EntryStatus.APPROVED);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntryWithProject(id, firmId, projectId, EntryStatus.APPROVED);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).getProjectId()).isEqualTo(projectId);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_filterWithStatus_WHEN_search_THEN_filtersCorrectly() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final EntryStatus status = EntryStatus.SUBMITTED;
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.of(status), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntity(id, firmId, EntryStatus.SUBMITTED);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntry(id, firmId, EntryStatus.SUBMITTED);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).getStatus()).isEqualTo(EntryStatus.SUBMITTED);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_filterWithOwnerId_WHEN_search_THEN_filtersCorrectly() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final UUID ownerId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(ownerId));
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntityWithOwner(id, firmId, ownerId, EntryStatus.DRAFT);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntryWithOwner(id, firmId, ownerId, EntryStatus.DRAFT);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).getUserId()).isEqualTo(ownerId);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_allFilters_WHEN_search_THEN_appliesAllFilters() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final UUID customerId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();
            final UUID ownerId = UUID.randomUUID();
            final EntryStatus status = EntryStatus.APPROVED;
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.of(customerId), Optional.of(projectId), Optional.of(status), Optional.of(ownerId));
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntityWithAll(id, firmId, customerId, projectId, ownerId, EntryStatus.APPROVED);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntryWithAll(id, firmId, customerId, projectId, ownerId, EntryStatus.APPROVED);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).getCustomerId()).isEqualTo(customerId);
            assertThat(result.items().get(0).getProjectId()).isEqualTo(projectId);
            assertThat(result.items().get(0).getStatus()).isEqualTo(EntryStatus.APPROVED);
            assertThat(result.items().get(0).getUserId()).isEqualTo(ownerId);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_validCursor_WHEN_search_THEN_paginatesCorrectly() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final UUID cursorId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, cursorId.toString());

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntity(id, firmId, EntryStatus.DRAFT);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntry(id, firmId, EntryStatus.DRAFT);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_fullPage_WHEN_search_THEN_hasMoreIsTrue() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(2, null);

            final UUID id1 = UUID.randomUUID();
            final UUID id2 = UUID.randomUUID();
            final TimeEntryEntity entity1 = createTimeEntryEntity(id1, firmId, EntryStatus.DRAFT);
            final TimeEntryEntity entity2 = createTimeEntryEntity(id2, firmId, EntryStatus.SUBMITTED);
            final List<TimeEntryEntity> entities = List.of(entity1, entity2);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);

            final TimeEntry domain1 = createTimeEntry(id1, firmId, EntryStatus.DRAFT);
            final TimeEntry domain2 = createTimeEntry(id2, firmId, EntryStatus.SUBMITTED);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity1)).thenReturn(domain1);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity2)).thenReturn(domain2);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(2);
            assertThat(result.hasMore()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(id2.toString());

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_partialPage_WHEN_search_THEN_hasMoreIsFalse() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntity(id, firmId, EntryStatus.DRAFT);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntry(id, firmId, EntryStatus.DRAFT);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.hasMore()).isFalse();
            assertThat(result.nextCursor()).isNull();

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_emptyResults_WHEN_search_THEN_returnsEmptyPage() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final Page<TimeEntryEntity> page = new PageImpl<>(List.of());

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).isEmpty();
            assertThat(result.totalItems()).isZero();
            assertThat(result.hasMore()).isFalse();
            assertThat(result.nextCursor()).isNull();

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }

    @Test
    void GIVEN_nullCursor_WHEN_search_THEN_startsFromBeginning() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, null);

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntity(id, firmId, EntryStatus.DRAFT);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntry(id, firmId, EntryStatus.DRAFT);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_blankCursor_WHEN_search_THEN_startsFromBeginning() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, "   ");

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntity(id, firmId, EntryStatus.DRAFT);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntry(id, firmId, EntryStatus.DRAFT);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void GIVEN_emptyCursor_WHEN_search_THEN_startsFromBeginning() {
        try (final MockedStatic<TimeEntryMapper> mapperMock = mockStatic(TimeEntryMapper.class)) {
            // GIVEN
            final UUID firmId = UUID.randomUUID();
            final TimeEntryFilter filter = new TimeEntryFilter(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            final com.aequitas.aequitascentralservice.domain.pagination.PageRequest pageRequest = 
                    new com.aequitas.aequitascentralservice.domain.pagination.PageRequest(10, "");

            final UUID id = UUID.randomUUID();
            final TimeEntryEntity entity = createTimeEntryEntity(id, firmId, EntryStatus.DRAFT);
            final List<TimeEntryEntity> entities = List.of(entity);
            final Page<TimeEntryEntity> page = new PageImpl<>(entities);
            final TimeEntry domain = createTimeEntry(id, firmId, EntryStatus.DRAFT);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            mapperMock.when(() -> TimeEntryMapper.toDomain(entity)).thenReturn(domain);

            // WHEN
            final PageResult<TimeEntry> result = adapter.search(firmId, filter, pageRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(repository);
        }
    }

    // ==================== Helper Methods ====================

    private TimeEntry createTimeEntry(final UUID id, final UUID firmId, final EntryStatus status) {
        return TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntryEntity createTimeEntryEntity(final UUID id, final UUID firmId, final EntryStatus status) {
        return TimeEntryEntity.builder()
                .id(id)
                .firmId(firmId)
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntry createCompleteTimeEntry(final UUID id, final UUID firmId, final UUID matterId,
            final UUID approvedBy, final Instant approvedAt) {
        return TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .matterId(matterId)
                .narrative("Complete entry")
                .durationMinutes(120)
                .status(EntryStatus.APPROVED)
                .createdAt(Instant.now())
                .updatedAt(approvedAt)
                .approvedBy(approvedBy)
                .approvedAt(approvedAt)
                .build();
    }

    private TimeEntryEntity createCompleteTimeEntryEntity(final UUID id, final UUID firmId, final UUID matterId,
            final UUID approvedBy, final Instant approvedAt) {
        return TimeEntryEntity.builder()
                .id(id)
                .firmId(firmId)
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .matterId(matterId)
                .narrative("Complete entry")
                .durationMinutes(120)
                .status(EntryStatus.APPROVED)
                .createdAt(Instant.now())
                .updatedAt(approvedAt)
                .approvedBy(approvedBy)
                .approvedAt(approvedAt)
                .build();
    }

    private TimeEntry createTimeEntryWithCustomer(final UUID id, final UUID firmId, final UUID customerId, final EntryStatus status) {
        return TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(UUID.randomUUID())
                .customerId(customerId)
                .projectId(UUID.randomUUID())
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntryEntity createTimeEntryEntityWithCustomer(final UUID id, final UUID firmId, final UUID customerId, final EntryStatus status) {
        return TimeEntryEntity.builder()
                .id(id)
                .firmId(firmId)
                .userId(UUID.randomUUID())
                .customerId(customerId)
                .projectId(UUID.randomUUID())
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntry createTimeEntryWithProject(final UUID id, final UUID firmId, final UUID projectId, final EntryStatus status) {
        return TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(projectId)
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntryEntity createTimeEntryEntityWithProject(final UUID id, final UUID firmId, final UUID projectId, final EntryStatus status) {
        return TimeEntryEntity.builder()
                .id(id)
                .firmId(firmId)
                .userId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .projectId(projectId)
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntry createTimeEntryWithOwner(final UUID id, final UUID firmId, final UUID ownerId, final EntryStatus status) {
        return TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(ownerId)
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntryEntity createTimeEntryEntityWithOwner(final UUID id, final UUID firmId, final UUID ownerId, final EntryStatus status) {
        return TimeEntryEntity.builder()
                .id(id)
                .firmId(firmId)
                .userId(ownerId)
                .customerId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntry createTimeEntryWithAll(final UUID id, final UUID firmId, final UUID customerId,
            final UUID projectId, final UUID ownerId, final EntryStatus status) {
        return TimeEntry.builder()
                .id(id)
                .firmId(firmId)
                .userId(ownerId)
                .customerId(customerId)
                .projectId(projectId)
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }

    private TimeEntryEntity createTimeEntryEntityWithAll(final UUID id, final UUID firmId, final UUID customerId,
            final UUID projectId, final UUID ownerId, final EntryStatus status) {
        return TimeEntryEntity.builder()
                .id(id)
                .firmId(firmId)
                .userId(ownerId)
                .customerId(customerId)
                .projectId(projectId)
                .matterId(null)
                .narrative("Test narrative")
                .durationMinutes(60)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .approvedBy(null)
                .approvedAt(null)
                .build();
    }
}
