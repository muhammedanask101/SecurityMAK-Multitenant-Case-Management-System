package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔐 AUTHENTICATION ONLY (tenant not resolved yet)
    Optional<User> findByEmail(String email);

    // 🔐 TENANT-SAFE METHODS
    List<User> findByTenantId(Long tenantId);

    Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);
}
