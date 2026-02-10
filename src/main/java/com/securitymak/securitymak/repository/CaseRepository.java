package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.Case;
import org.springframework.data.jpa.repository.JpaRepository;

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
    
}
