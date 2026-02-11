package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.CaseComment;
import com.securitymak.securitymak.model.SensitivityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CaseCommentRepository extends JpaRepository<CaseComment, Long> {

    @Query("""
    SELECT c
    FROM CaseComment c
    WHERE c.caseEntity.id = :caseId
      AND c.caseEntity.tenantId = :tenantId
      AND c.sensitivityLevel <= :clearance
    ORDER BY c.createdAt ASC
""")
List<CaseComment> findAccessibleComments(
        @Param("caseId") Long caseId,
        @Param("tenantId") Long tenantId,
        @Param("clearance") SensitivityLevel clearance
);
}