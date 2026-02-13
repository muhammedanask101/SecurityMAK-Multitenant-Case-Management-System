package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.InviteView;
import com.securitymak.securitymak.exception.BadRequestException;
import com.securitymak.securitymak.exception.UnauthorizedException;
import com.securitymak.securitymak.model.*;
import com.securitymak.securitymak.repository.EmailBanRepository;
import com.securitymak.securitymak.repository.InviteRepository;
import com.securitymak.securitymak.repository.RoleRepository;
import com.securitymak.securitymak.repository.UserRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
private final EmailBanRepository emailBanRepository;

    @Transactional
    public InviteView createInvite(
            String email,
            String roleName,
            SensitivityLevel clearanceLevel
    ) {

        User currentAdmin = SecurityUtils.getCurrentUser();

        if (!"ADMIN".equals(currentAdmin.getRole().getName())) {
            throw new UnauthorizedException("Only ADMIN can create invites");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() ->
                        new BadRequestException("Role not found"));

        Invite invite = Invite.builder()
                .email(email)
                .tenant(currentAdmin.getTenant())
                .role(role)
                .clearanceLevel(clearanceLevel)
                .createdBy(currentAdmin)
                .status(InviteStatus.PENDING)
                .build();

        Invite saved = inviteRepository.save(invite);

        auditService.log(
                currentAdmin.getEmail(),
                AuditAction.INVITE_CREATED,
                "INVITE",
                saved.getId(),
                null,
                email,
                currentAdmin.getTenant().getId()
        );

        return toView(saved);
    }

    @Transactional
    public void terminateInvite(Long inviteId) {

        User currentAdmin = SecurityUtils.getCurrentUser();

        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() ->
                        new BadRequestException("Invite not found"));

        if (!invite.getTenant().getId()
                .equals(currentAdmin.getTenant().getId())) {

            throw new UnauthorizedException("Cross-tenant access denied");
        }

        if (invite.getStatus() == InviteStatus.APPROVED) {
            throw new BadRequestException(
                    "Cannot terminate an approved invite");
        }

        invite.setStatus(InviteStatus.TERMINATED);
        invite.setTerminatedAt(Instant.now());
        invite.setTerminatedBy(currentAdmin);

        inviteRepository.save(invite);

        auditService.log(
                currentAdmin.getEmail(),
                AuditAction.INVITE_TERMINATED,
                "INVITE",
                invite.getId(),
                null,
                invite.getEmail(),
                currentAdmin.getTenant().getId()
        );
    }

@Transactional
public void registerViaInvite(
        String token,
        String email,
        String rawPassword
) {
        

    Invite invite = inviteRepository.findByToken(token)
            .orElseThrow(() ->
                    new BadRequestException("Invalid invite token"));

        if (invite.getStatus() == InviteStatus.REGISTERED) {
                throw new BadRequestException("Already registered. Awaiting approval.");
        }

    if (invite.getStatus() != InviteStatus.PENDING) {
        auditService.log(
                email,
                AuditAction.INVALID_INVITE_TOKEN_USED,
                "INVITE",
                invite.getId(),
                null,
                "STATUS=" + invite.getStatus(),
                invite.getTenant().getId()
        );
        throw new BadRequestException("Invite is not valid");
    }

    if (!invite.getEmail().equalsIgnoreCase(email)) {
        auditService.log(
                email,
                AuditAction.INVITE_EMAIL_MISMATCH,
                "INVITE",
                invite.getId(),
                invite.getEmail(),
                email,
                invite.getTenant().getId()
        );
        throw new BadRequestException("Email does not match invite");
    }

    if (emailBanRepository.existsByEmailAndTenant_Id(
            email,
            invite.getTenant().getId()
    )) {
        auditService.log(
                email,
                AuditAction.BANNED_USER_ATTEMPTED_REGISTRATION,
                "USER",
                null,
                null,
                "TENANT_BAN",
                invite.getTenant().getId()
        );

        throw new UnauthorizedException(
                "Email is banned for this organization"
        );
    }

    if (userRepository.existsByEmail(email)) {
        throw new BadRequestException("Email already registered");
    }

    User newUser = User.builder()
            .email(email)
            .password(passwordEncoder.encode(rawPassword))
            .role(invite.getRole())
            .tenant(invite.getTenant())
            .clearanceLevel(invite.getClearanceLevel())
            .enabled(false)
            .joinedViaInvite(invite)
            .build();

    userRepository.save(newUser);

    invite.setStatus(InviteStatus.REGISTERED);
    invite.setRegisteredAt(java.time.Instant.now());

    inviteRepository.save(invite);

    auditService.log(
            email,
            AuditAction.INVITE_REGISTERED,
            "USER",
            newUser.getId(),
            null,
            "INVITE_ID=" + invite.getId(),
            invite.getTenant().getId()
    );
}

@Transactional
public void approveInvite(Long inviteId) {

    User currentAdmin = SecurityUtils.getCurrentUser();

    Invite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() ->
                    new BadRequestException("Invite not found"));

    if (!invite.getTenant().getId()
            .equals(currentAdmin.getTenant().getId())) {

        throw new UnauthorizedException("Cross-tenant access denied");
    }

    if (!"ADMIN".equals(currentAdmin.getRole().getName())) {
        throw new UnauthorizedException("Only ADMIN can approve invites");
    }

    if (invite.getStatus() != InviteStatus.REGISTERED) {
        throw new BadRequestException(
                "Invite is not awaiting approval");
    }

    User invitedUser = userRepository.findByEmail(invite.getEmail())
            .orElseThrow(() ->
                    new BadRequestException("User not found"));

    invitedUser.setEnabled(true);

    invite.setStatus(InviteStatus.APPROVED);
    invite.setApprovedAt(java.time.Instant.now());

    userRepository.save(invitedUser);
    inviteRepository.save(invite);

    auditService.log(
            currentAdmin.getEmail(),
            AuditAction.INVITE_APPROVED,
            "USER",
            invitedUser.getId(),
            null,
            "INVITE_ID=" + invite.getId(),
            currentAdmin.getTenant().getId()
    );
}

@Transactional
public void rejectInvite(Long inviteId) {

    User currentAdmin = SecurityUtils.getCurrentUser();

    Invite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() ->
                    new BadRequestException("Invite not found"));

    if (!invite.getTenant().getId()
            .equals(currentAdmin.getTenant().getId())) {

        throw new UnauthorizedException("Cross-tenant access denied");
    }

    if (!"ADMIN".equals(currentAdmin.getRole().getName())) {
        throw new UnauthorizedException("Only ADMIN can reject invites");
    }

    if (invite.getStatus() != InviteStatus.REGISTERED) {
        throw new BadRequestException(
                "Invite is not awaiting approval");
    }

    User invitedUser = userRepository.findByEmail(invite.getEmail())
            .orElseThrow(() ->
                    new BadRequestException("User not found"));

    invitedUser.setEnabled(false);

    invite.setStatus(InviteStatus.REJECTED);

    userRepository.save(invitedUser);
    inviteRepository.save(invite);

    auditService.log(
            currentAdmin.getEmail(),
            AuditAction.INVITE_REJECTED,
            "USER",
            invitedUser.getId(),
            null,
            "INVITE_ID=" + invite.getId(),
            currentAdmin.getTenant().getId()
    );
}

public org.springframework.data.domain.Page<InviteView> getInvites(
        InviteStatus status,
        org.springframework.data.domain.Pageable pageable
) {

    User currentUser = SecurityUtils.getCurrentUser();

    if (!"ADMIN".equals(currentUser.getRole().getName())) {
        throw new UnauthorizedException("Only ADMIN can view invites");
    }

    Long tenantId = currentUser.getTenant().getId();

    org.springframework.data.domain.Page<Invite> page;

    if (status != null) {
        page = inviteRepository
                .findByTenant_IdAndStatus(tenantId, status, pageable);
    } else {
        page = inviteRepository
                .findByTenant_Id(tenantId, pageable);
    }

    return page.map(this::toView);
}

@Transactional(readOnly = true)
public InviteView getInviteById(Long inviteId) {

    User currentUser = SecurityUtils.getCurrentUser();

    if (!"ADMIN".equals(currentUser.getRole().getName())) {
        throw new UnauthorizedException("Only ADMIN can view invite details");
    }

    Invite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() ->
                    new BadRequestException("Invite not found"));

    if (!invite.getTenant().getId()
            .equals(currentUser.getTenant().getId())) {
        throw new UnauthorizedException("Cross-tenant access denied");
    }

    return toView(invite);
}

private InviteView toView(Invite invite) {

    return new InviteView(
            invite.getId(),
            invite.getEmail(),
            invite.getRole().getName(),
            invite.getClearanceLevel(),
            invite.getStatus(),
            invite.getCreatedBy().getEmail(),
            invite.getCreatedAt(),
            invite.getRegisteredAt(),
            invite.getApprovedAt(),
            invite.getTerminatedAt()
    );
}
}
