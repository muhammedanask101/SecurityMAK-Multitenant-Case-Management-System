package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.Case;
import com.securitymak.securitymak.model.SensitivityLevel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<Case, Long> {

    List<Case> findAllByTenantId(Long tenantId);

    List<Case> findByTenantIdAndOwnerId(Long tenantId, Long ownerId);

    Optional<Case> findByIdAndTenantId(Long id, Long tenantId);

    Page<Case> findAllByTenantId(Long tenantId, Pageable pageable);
    
    Page<Case> findByTenantIdAndOwnerId(Long tenantId, Long ownerId, Pageable pageable);
    
    Page<Case> findByTenantIdAndSensitivityLevelLessThanEqual(
        Long tenantId,
        SensitivityLevel level,
        Pageable pageable
    );

    @Query("""
    SELECT c
    FROM Case c
    WHERE c.tenantId = :tenantId
        AND c.sensitivityLevel.level <= :clearanceLevel
    """)
    Page<Case> findAccessibleCases(
            @Param("tenantId") Long tenantId,
            @Param("clearanceLevel") int clearanceLevel,
            Pageable pageable
    );
}
