package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.Case;
import com.securitymak.securitymak.model.CaseComment;
import com.securitymak.securitymak.model.CaseStatus;
import com.securitymak.securitymak.model.SensitivityLevel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CaseRepository extends
        JpaRepository<Case, Long>,
        JpaSpecificationExecutor<Case> {


    List<Case> findAllByTenantId(Long tenantId);

    List<Case> findByTenantIdAndOwnerId(Long tenantId, Long ownerId);

    Optional<Case> findByIdAndTenantId(Long id, Long tenantId);

    Page<Case> findAllByTenantId(Long tenantId, Pageable pageable);
    
    Page<Case> findByTenantIdAndOwnerId(Long tenantId, Long ownerId, Pageable pageable);
    

    @Query("""
    SELECT c
    FROM CaseComment c
    JOIN FETCH c.author a
    JOIN FETCH c.caseEntity ce
    WHERE ce.id = :caseId
      AND ce.tenantId = :tenantId
    ORDER BY c.createdAt ASC
""")
List<CaseComment> findAccessibleComments(
        @Param("caseId") Long caseId,
        @Param("tenantId") Long tenantId
);

Page<Case> findByTenantIdAndTitleContainingIgnoreCase(
        Long tenantId,
        String title,
        Pageable pageable
);

Page<Case> findByTenantIdAndStatus(
        Long tenantId,
        CaseStatus status,
        Pageable pageable
);

Page<Case> findByTenantIdAndSensitivityLevel(
        Long tenantId,
        SensitivityLevel sensitivityLevel,
        Pageable pageable
);

}
