package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.Case;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseRepository extends JpaRepository<Case, Long> {

    List<Case> findByTenantId(Long tenantId);

    List<Case> findByTenantIdAndOwnerId(Long tenantId, Long ownerId);
}
