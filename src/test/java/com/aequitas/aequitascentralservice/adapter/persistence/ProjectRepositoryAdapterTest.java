package com.aequitas.aequitascentralservice.adapter.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.ProjectEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.ProjectJpaRepository;
import com.aequitas.aequitascentralservice.domain.model.Project;

/**
 * Comprehensive test suite for {@link ProjectRepositoryAdapter}.
 * Targets: 100% Line Coverage, 100% Branch Coverage, 100% Mutation Score.
 */
@ExtendWith(MockitoExtension.class)
class ProjectRepositoryAdapterTest {

    @Mock
    private ProjectJpaRepository mockRepository;

    private ProjectRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProjectRepositoryAdapter(mockRepository);
    }

    @Test
    void GIVEN_existingProjectEntityInDatabase_WHEN_findById_THEN_returnsPopulatedOptionalWithDomainProject() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String name = "Test Project";
        String status = "ACTIVE";
        Instant createdAt = Instant.now();

        ProjectEntity entity = ProjectEntity.builder()
                .id(projectId)
                .firmId(firmId)
                .customerId(customerId)
                .name(name)
                .status(status)
                .createdAt(createdAt)
                .build();

        Project expectedDomain = Project.builder()
                .id(projectId)
                .firmId(firmId)
                .customerId(customerId)
                .name(name)
                .status(status)
                .createdAt(createdAt)
                .build();

        when(mockRepository.findByIdAndFirmId(projectId, firmId))
                .thenReturn(Optional.of(entity));

        // WHEN
        Optional<Project> result = adapter.findById(projectId, firmId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDomain);
        assertThat(result.get().id()).isEqualTo(projectId);
        assertThat(result.get().firmId()).isEqualTo(firmId);
        assertThat(result.get().customerId()).isEqualTo(customerId);
        assertThat(result.get().name()).isEqualTo(name);
        assertThat(result.get().status()).isEqualTo(status);
        assertThat(result.get().createdAt()).isEqualTo(createdAt);
        
        verify(mockRepository).findByIdAndFirmId(projectId, firmId);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    void GIVEN_noMatchingProjectInDatabase_WHEN_findById_THEN_returnsEmptyOptional() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();

        when(mockRepository.findByIdAndFirmId(projectId, firmId))
                .thenReturn(Optional.empty());

        // WHEN
        Optional<Project> result = adapter.findById(projectId, firmId);

        // THEN
        assertThat(result).isEmpty();
        verify(mockRepository).findByIdAndFirmId(projectId, firmId);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    void GIVEN_projectWithDifferentFirmId_WHEN_findById_THEN_returnsEmptyOptional() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID differentFirmId = UUID.randomUUID();

        // Repository returns empty because firm IDs don't match
        when(mockRepository.findByIdAndFirmId(projectId, differentFirmId))
                .thenReturn(Optional.empty());

        // WHEN
        Optional<Project> result = adapter.findById(projectId, differentFirmId);

        // THEN
        assertThat(result).isEmpty();
        verify(mockRepository).findByIdAndFirmId(projectId, differentFirmId);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    void GIVEN_projectWithInactiveStatus_WHEN_findById_THEN_returnsProjectWithCorrectStatus() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String name = "Inactive Project";
        String status = "INACTIVE";
        Instant createdAt = Instant.now();

        ProjectEntity entity = ProjectEntity.builder()
                .id(projectId)
                .firmId(firmId)
                .customerId(customerId)
                .name(name)
                .status(status)
                .createdAt(createdAt)
                .build();

        when(mockRepository.findByIdAndFirmId(projectId, firmId))
                .thenReturn(Optional.of(entity));

        // WHEN
        Optional<Project> result = adapter.findById(projectId, firmId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo("INACTIVE");
        verify(mockRepository).findByIdAndFirmId(projectId, firmId);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    void GIVEN_projectWithLongName_WHEN_findById_THEN_preservesFullNameInDomainObject() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String longName = "A".repeat(255);
        String status = "ACTIVE";
        Instant createdAt = Instant.now();

        ProjectEntity entity = ProjectEntity.builder()
                .id(projectId)
                .firmId(firmId)
                .customerId(customerId)
                .name(longName)
                .status(status)
                .createdAt(createdAt)
                .build();

        when(mockRepository.findByIdAndFirmId(projectId, firmId))
                .thenReturn(Optional.of(entity));

        // WHEN
        Optional<Project> result = adapter.findById(projectId, firmId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().name()).hasSize(255);
        assertThat(result.get().name()).isEqualTo(longName);
        verify(mockRepository).findByIdAndFirmId(projectId, firmId);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    void GIVEN_projectWithMinimalTimeCreatedAt_WHEN_findById_THEN_preservesTimestamp() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Instant minTime = Instant.EPOCH;

        ProjectEntity entity = ProjectEntity.builder()
                .id(projectId)
                .firmId(firmId)
                .customerId(customerId)
                .name("Old Project")
                .status("ARCHIVED")
                .createdAt(minTime)
                .build();

        when(mockRepository.findByIdAndFirmId(projectId, firmId))
                .thenReturn(Optional.of(entity));

        // WHEN
        Optional<Project> result = adapter.findById(projectId, firmId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().createdAt()).isEqualTo(Instant.EPOCH);
        verify(mockRepository).findByIdAndFirmId(projectId, firmId);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    void GIVEN_projectWithMaximalTimeCreatedAt_WHEN_findById_THEN_preservesTimestamp() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Instant maxTime = Instant.parse("2999-12-31T23:59:59Z");

        ProjectEntity entity = ProjectEntity.builder()
                .id(projectId)
                .firmId(firmId)
                .customerId(customerId)
                .name("Future Project")
                .status("PLANNED")
                .createdAt(maxTime)
                .build();

        when(mockRepository.findByIdAndFirmId(projectId, firmId))
                .thenReturn(Optional.of(entity));

        // WHEN
        Optional<Project> result = adapter.findById(projectId, firmId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().createdAt()).isEqualTo(maxTime);
        verify(mockRepository).findByIdAndFirmId(projectId, firmId);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    void GIVEN_multipleCallsWithSameIds_WHEN_findById_THEN_eachCallInvokesRepository() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        ProjectEntity entity = ProjectEntity.builder()
                .id(projectId)
                .firmId(firmId)
                .customerId(customerId)
                .name("Project")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        when(mockRepository.findByIdAndFirmId(projectId, firmId))
                .thenReturn(Optional.of(entity));

        // WHEN
        Optional<Project> result1 = adapter.findById(projectId, firmId);
        Optional<Project> result2 = adapter.findById(projectId, firmId);

        // THEN
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        verify(mockRepository, times(2)).findByIdAndFirmId(projectId, firmId);
        verifyNoMoreInteractions(mockRepository);
    }
}
