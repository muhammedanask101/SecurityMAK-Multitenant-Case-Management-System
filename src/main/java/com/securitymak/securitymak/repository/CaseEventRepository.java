package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.CaseEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseEventRepository extends JpaRepository<CaseEvent, Long> {

    List<CaseEvent> findByTenantIdAndCaseEntityIdAndActiveTrueOrderByEventDateAsc(
        Long tenantId,
        Long caseId
);
}

