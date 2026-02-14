package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.CaseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseAssignmentRepository extends JpaRepository<CaseAssignment, Long> {

    List<CaseAssignment> findByTenantIdAndCaseEntityId(Long tenantId, Long caseId);

    boolean existsByTenantIdAndCaseEntityIdAndUserIdAndRole(
            Long tenantId,
            Long caseId,
            Long userId,
            com.securitymak.securitymak.model.AssignmentRole role
    );
}
