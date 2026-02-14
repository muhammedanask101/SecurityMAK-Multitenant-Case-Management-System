package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.CaseDocument;
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

}
