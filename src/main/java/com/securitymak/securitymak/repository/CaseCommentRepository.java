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
    JOIN FETCH c.author a
    JOIN FETCH c.caseEntity ce
    WHERE ce.id = :caseId
      AND ce.tenantId = :tenantId
    ORDER BY c.createdAt ASC
""")
List<CaseComment> findAllCommentsForCase(
        @Param("caseId") Long caseId,
        @Param("tenantId") Long tenantId
);
}