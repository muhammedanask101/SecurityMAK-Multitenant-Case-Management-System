package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.Invite;
import com.securitymak.securitymak.model.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {

    Optional<Invite> findByToken(String token);

    List<Invite> findByTenant_Id(Long tenantId);

    List<Invite> findByTenant_IdAndStatus(Long tenantId, InviteStatus status);

    Page<Invite> findByTenant_Id(Long tenantId, Pageable pageable);

Page<Invite> findByTenant_IdAndStatus(
        Long tenantId,
        InviteStatus status,
        Pageable pageable
);

}
