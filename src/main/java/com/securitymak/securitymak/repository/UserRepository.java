package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔐 AUTHENTICATION ONLY
    Optional<User> findByEmail(String email);

    // 🔐 TENANT-SAFE METHODS (CORRECT FOR @ManyToOne Tenant)
    List<User> findAllByTenant_Id(Long tenantId);

    Optional<User> findByIdAndTenant_Id(Long id, Long tenantId);

    Optional<User> findByEmailAndTenant_Id(String email, Long tenantId);

    boolean existsByEmailAndTenant_Id(String email, Long tenantId);
}
