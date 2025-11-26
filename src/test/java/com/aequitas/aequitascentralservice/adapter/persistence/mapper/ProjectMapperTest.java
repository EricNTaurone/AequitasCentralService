package com.aequitas.aequitascentralservice.adapter.persistence.mapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.ProjectEntity;
import com.aequitas.aequitascentralservice.domain.model.Project;

class ProjectMapperTest {

    @Test
    void GIVEN_validProjectEntity_WHEN_toDomain_THEN_mapsAllFieldsCorrectly() {
        // GIVEN
        UUID id = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String name = "Enterprise Integration Project";
        String status = "ACTIVE";
        Instant createdAt = Instant.parse("2025-01-10T09:00:00Z");

        ProjectEntity entity = ProjectEntity.builder()
                .id(id)
                .firmId(firmId)
                .customerId(customerId)
                .name(name)
                .status(status)
                .createdAt(createdAt)
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project).isNotNull();
        assertThat(project.id()).isEqualTo(id);
        assertThat(project.firmId()).isEqualTo(firmId);
        assertThat(project.customerId()).isEqualTo(customerId);
        assertThat(project.name()).isEqualTo(name);
        assertThat(project.status()).isEqualTo(status);
        assertThat(project.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void GIVEN_projectEntityWithDifferentStatus_WHEN_toDomain_THEN_mapsStatusCorrectly() {
        // GIVEN
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .name("Archived Project")
                .status("ARCHIVED")
                .createdAt(Instant.now())
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project.status()).isEqualTo("ARCHIVED");
    }

    @Test
    void GIVEN_projectEntityWithInactiveStatus_WHEN_toDomain_THEN_mapsStatusCorrectly() {
        // GIVEN
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .name("Inactive Project")
                .status("INACTIVE")
                .createdAt(Instant.now())
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project.status()).isEqualTo("INACTIVE");
    }

    @Test
    void GIVEN_projectEntityWithLongName_WHEN_toDomain_THEN_preservesFullName() {
        // GIVEN
        String longName = "A".repeat(255);
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .name(longName)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project.name()).isEqualTo(longName);
        assertThat(project.name()).hasSize(255);
    }

    @Test
    void GIVEN_projectEntityWithSpecialCharactersInName_WHEN_toDomain_THEN_preservesSpecialCharacters() {
        // GIVEN
        String nameWithSpecialChars = "Project: Test & Development (Phase-1) – V2.0";
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .name(nameWithSpecialChars)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project.name()).isEqualTo(nameWithSpecialChars);
    }

    @Test
    void GIVEN_projectEntityWithEmptyName_WHEN_toDomain_THEN_mapsEmptyName() {
        // GIVEN
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .name("")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project.name()).isEmpty();
    }

    @Test
    void GIVEN_projectEntityWithVeryRecentTimestamp_WHEN_toDomain_THEN_preservesNanosecondPrecision() {
        // GIVEN
        Instant preciseTime = Instant.parse("2025-11-22T15:30:45.123456789Z");
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .name("Timestamp Test")
                .status("ACTIVE")
                .createdAt(preciseTime)
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project.createdAt()).isEqualTo(preciseTime);
        assertThat(project.createdAt().getNano()).isEqualTo(123456789);
    }

    @Test
    void GIVEN_projectEntityWithOldTimestamp_WHEN_toDomain_THEN_preservesHistoricalDate() {
        // GIVEN
        Instant oldTime = Instant.parse("2020-01-01T00:00:00Z");
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .name("Legacy Project")
                .status("ARCHIVED")
                .createdAt(oldTime)
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project.createdAt()).isEqualTo(oldTime);
    }

    @Test
    void GIVEN_twoProjectEntitiesWithSameFirmId_WHEN_toDomain_THEN_bothMapToSameFirmId() {
        // GIVEN
        UUID sharedFirmId = UUID.randomUUID();
        
        ProjectEntity entity1 = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(sharedFirmId)
                .customerId(UUID.randomUUID())
                .name("Project A")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        ProjectEntity entity2 = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(sharedFirmId)
                .customerId(UUID.randomUUID())
                .name("Project B")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        // WHEN
        Project project1 = ProjectMapper.toDomain(entity1);
        Project project2 = ProjectMapper.toDomain(entity2);

        // THEN
        assertThat(project1.firmId()).isEqualTo(sharedFirmId);
        assertThat(project2.firmId()).isEqualTo(sharedFirmId);
        assertThat(project1.firmId()).isEqualTo(project2.firmId());
    }

    @Test
    void GIVEN_twoProjectEntitiesWithSameCustomerId_WHEN_toDomain_THEN_bothMapToSameCustomerId() {
        // GIVEN
        UUID sharedCustomerId = UUID.randomUUID();
        
        ProjectEntity entity1 = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(sharedCustomerId)
                .name("Customer Project 1")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        ProjectEntity entity2 = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .firmId(UUID.randomUUID())
                .customerId(sharedCustomerId)
                .name("Customer Project 2")
                .status("INACTIVE")
                .createdAt(Instant.now())
                .build();

        // WHEN
        Project project1 = ProjectMapper.toDomain(entity1);
        Project project2 = ProjectMapper.toDomain(entity2);

        // THEN
        assertThat(project1.customerId()).isEqualTo(sharedCustomerId);
        assertThat(project2.customerId()).isEqualTo(sharedCustomerId);
        assertThat(project1.customerId()).isEqualTo(project2.customerId());
    }

    @Test
    void GIVEN_projectEntityWithMinimalData_WHEN_toDomain_THEN_mapsAllFieldsIncludingMinimalValues() {
        // GIVEN
        UUID id = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        ProjectEntity entity = ProjectEntity.builder()
                .id(id)
                .firmId(firmId)
                .customerId(customerId)
                .name("X")
                .status("A")
                .createdAt(Instant.EPOCH)
                .build();

        // WHEN
        Project project = ProjectMapper.toDomain(entity);

        // THEN
        assertThat(project.id()).isEqualTo(id);
        assertThat(project.firmId()).isEqualTo(firmId);
        assertThat(project.customerId()).isEqualTo(customerId);
        assertThat(project.name()).isEqualTo("X");
        assertThat(project.status()).isEqualTo("A");
        assertThat(project.createdAt()).isEqualTo(Instant.EPOCH);
    }
}
