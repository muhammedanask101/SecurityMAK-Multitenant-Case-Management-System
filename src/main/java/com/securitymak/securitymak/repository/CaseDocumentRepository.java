package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.CaseDocument;
import com.securitymak.securitymak.model.SensitivityLevel;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

public interface CaseDocumentRepository extends JpaRepository<CaseDocument, Long> {

    List<CaseDocument> findByTenantIdAndCaseEntityIdAndActiveTrue(
        Long tenantId,
        Long caseId
);

Optional<CaseDocument> findTopByDocumentGroupIdOrderByVersionDesc(String documentGroupId);

Optional<CaseDocument> findByCaseEntityIdAndFileHashAndActiveTrue(
        Long caseId,
        String fileHash
);

List<CaseDocument> findByTenantIdAndCaseEntityIdAndActiveTrueAndSensitivityLevelLessThanEqual(
    Long tenantId,
    Long caseId,
    SensitivityLevel sensitivityLevel
);

Optional<CaseDocument> findByIdAndTenantIdAndActiveTrue(
        Long id,
        Long tenantId
);

Optional<CaseDocument> 
findByTenantIdAndCaseEntityIdAndFileHashAndActiveTrue(
        Long tenantId,
        Long caseId,
        String fileHash
);

List<CaseDocument> findByTenantIdAndCaseEntityIdAndDocumentGroupIdAndActiveTrue(
        Long tenantId,
        Long caseId,
        String documentGroupId
);

}
