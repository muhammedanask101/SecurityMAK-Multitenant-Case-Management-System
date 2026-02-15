package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.EmailBan;
import com.securitymak.securitymak.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailBanRepository extends JpaRepository<EmailBan, Long> {

    Optional<EmailBan> findByEmailAndTenant(String email, Tenant tenant);

    void deleteByEmailAndTenant(String email, Tenant tenant);

    boolean existsByEmailAndTenantIsNull(String email);


    boolean existsByEmailAndTenant(String email, Tenant tenant);


    boolean existsByEmailAndTenant_Id(String email, Long tenantId);
}
