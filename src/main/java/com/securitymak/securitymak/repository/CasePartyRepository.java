package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.CaseParty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CasePartyRepository extends JpaRepository<CaseParty, Long> {

    List<CaseParty> findByTenantIdAndCaseEntityId(Long tenantId, Long caseId);

}
