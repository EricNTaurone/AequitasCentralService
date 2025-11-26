package com.aequitas.aequitascentralservice.adapter.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.FirmEntity;

/**
 * Spring Data JPA repository for firm persistence.
 */
@Repository
public interface FirmJpaRepository extends JpaRepository<FirmEntity, UUID>, JpaSpecificationExecutor<FirmEntity> {
}
