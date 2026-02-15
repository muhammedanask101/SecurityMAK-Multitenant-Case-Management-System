package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.UserAdminView;
import com.securitymak.securitymak.exception.BusinessRuleViolationException;
import com.securitymak.securitymak.exception.ResourceNotFoundException;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import com.securitymak.securitymak.exception.UnauthorizedException;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.EmailBan;
import com.securitymak.securitymak.model.Role;
import com.securitymak.securitymak.model.SensitivityLevel;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.EmailBanRepository;
import com.securitymak.securitymak.repository.RoleRepository;
import com.securitymak.securitymak.repository.UserRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.securitymak.securitymak.dto.AuditLogView;
import com.securitymak.securitymak.dto.UpdateClearanceRequest;
import org.springframework.transaction.annotation.Transactional;
import com.securitymak.securitymak.exception.BadRequestException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final EmailBanRepository emailBanRepository;

    public List<UserAdminView> getAllUsers() {

        Long tenantId = SecurityUtils.getCurrentTenantId();

        auditService.log(
        SecurityUtils.getCurrentUserEmail(),
        AuditAction.ADMIN_OVERRIDE,
        "USER_LIST",
        null,
        null,
        null,
        tenantId
);

        return userRepository.findAllByTenant_Id(tenantId)
                .stream()
                .map(user -> new UserAdminView(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().getName(),
                        user.getClearanceLevel(),
                        user.getTenant().getName(),
                        user.isEnabled() 
                ))
                .toList();
    }

    public Page<AuditLogView> getAuditLogs(
            String actorEmail,
            AuditAction action,
            String targetType,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        return auditService.getAuditLogs(
                actorEmail,
                action,
                targetType,
                from,
                to,
                pageable
        );
    }

@Transactional
public UserAdminView updateUserRole(Long userId, String roleName) {

    Long tenantId = SecurityUtils.getCurrentTenantId();
    User currentAdmin = SecurityUtils.getCurrentUser();

    User user = userRepository
            .findByIdAndTenant_Id(userId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getEmail().equals(currentAdmin.getEmail())) {
        throw new BusinessRuleViolationException("Admins cannot change their own role");
    }

    Role newRole = roleRepository.findByName(roleName.toUpperCase())
            .orElseThrow(() -> new BusinessRuleViolationException("Role not found"));

    String oldRole = user.getRole().getName();

    if (oldRole.equals(newRole.getName())) {
        return toAdminView(user);
    }

    user.setRole(newRole);

    // 🔥 AUTO PROMOTE CLEARANCE
    if ("ADMIN".equals(newRole.getName())) {
        user.setClearanceLevel(SensitivityLevel.CRITICAL);
    }

    userRepository.save(user);

    auditService.log(
            currentAdmin.getEmail(),
            AuditAction.USER_ROLE_CHANGED,
            "USER",
            user.getId(),
            oldRole,
            newRole.getName(),
            tenantId
    );

    return toAdminView(user);
}

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    private UserAdminView toAdminView(User user) {
    return new UserAdminView(
            user.getId(),
            user.getEmail(),
            user.getRole().getName(),
            user.getClearanceLevel(),
            user.getTenant().getName(),
            user.isEnabled() 
    );
}

        @Transactional
        public UserAdminView updateUserClearance(
                Long userId,
                UpdateClearanceRequest request
        ) {

        SecurityUtils.requireAdmin();

        User currentAdmin = SecurityUtils.getCurrentUser();
        

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if ("ADMIN".equals(targetUser.getRole().getName())) {
                throw new BadRequestException("Cannot change ADMIN clearance level");
        }

        // 🔐 Tenant isolation
        if (!targetUser.getTenant().getId()
                .equals(currentAdmin.getTenant().getId())) {
                throw new UnauthorizedCaseAccessException(
                        "Cross-tenant modification attempt"
                );
        }

        SensitivityLevel oldLevel = targetUser.getClearanceLevel();

        targetUser.setClearanceLevel(request.clearanceLevel());

        userRepository.save(targetUser);

        auditService.log(
                currentAdmin.getEmail(),
                AuditAction.CLEARANCE_UPDATED,
                "USER",
                userId,
                oldLevel.name(),
                request.clearanceLevel().name(),
                currentAdmin.getTenant().getId()
        );

        return toAdminView(targetUser);
        }

        @Transactional
public void disableUser(Long userId) {

    User currentAdmin = SecurityUtils.getCurrentUser();

    if (!"ADMIN".equals(currentAdmin.getRole().getName())) {
        throw new UnauthorizedException("Only ADMIN allowed");
    }

    User target = userRepository.findById(userId)
            .orElseThrow(() -> new BadRequestException("User not found"));

            // Prevent disabling last ADMIN
if ("ADMIN".equals(target.getRole().getName())) {

    long adminCount = userRepository
            .countByTenant_IdAndRole_Name(
                    currentAdmin.getTenant().getId(),
                    "ADMIN"
            );

    if (adminCount <= 1) {
        throw new BadRequestException(
                "Cannot disable the last ADMIN"
        );
    }
}

    if (!target.getTenant().getId()
            .equals(currentAdmin.getTenant().getId())) {
        throw new UnauthorizedException("Cross-tenant access denied");
    }

    if (target.getId().equals(currentAdmin.getId())) {
        throw new BadRequestException("Cannot disable yourself");
    }

    target.setEnabled(false);

    auditService.log(
            currentAdmin.getEmail(),
            AuditAction.USER_DISABLED,
            "USER",
            target.getId(),
            "ENABLED=true",
            "ENABLED=false",
            currentAdmin.getTenant().getId()
    );
}

@Transactional
public void enableUser(Long userId) {

    User currentAdmin = SecurityUtils.getCurrentUser();

    if (!"ADMIN".equals(currentAdmin.getRole().getName())) {
        throw new UnauthorizedException("Only ADMIN allowed");
    }

    User target = userRepository.findById(userId)
            .orElseThrow(() -> new BadRequestException("User not found"));

    if (!target.getTenant().getId()
            .equals(currentAdmin.getTenant().getId())) {
        throw new UnauthorizedException("Cross-tenant access denied");
    }

    target.setEnabled(true);

    auditService.log(
            currentAdmin.getEmail(),
            AuditAction.USER_REENABLED,
            "USER",
            target.getId(),
            "ENABLED=false",
            "ENABLED=true",
            currentAdmin.getTenant().getId()
    );
}

@Transactional
public void banUser(Long userId, String reason) {

    User admin = SecurityUtils.getCurrentUser();

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.getTenant().getId().equals(admin.getTenant().getId())) {
        throw new UnauthorizedException("Cross-tenant access denied");
    }

    if (user.getId().equals(admin.getId())) {
        throw new BadRequestException("Cannot ban yourself");
    }

    if ("ADMIN".equals(user.getRole().getName())) {
        throw new BadRequestException("Admins cannot be banned");
    }


    if (emailBanRepository.existsByEmailAndTenant(user.getEmail(), user.getTenant())) {
        return; // already banned
    }

    EmailBan ban = EmailBan.builder()
            .email(user.getEmail())
            .tenant(user.getTenant())
            .bannedBy(admin)
            .reason(reason)
            .build();

    emailBanRepository.save(ban);

    user.setEnabled(false);

    auditService.log(
            admin.getEmail(),
            AuditAction.EMAIL_BANNED,
            "USER",
            user.getId(),
            null,
            reason,
            admin.getTenant().getId()
    );
}

@Transactional
public void unbanUser(Long userId) {

    User admin = SecurityUtils.getCurrentUser();

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

if ("ADMIN".equals(user.getRole().getName())) {
        throw new BadRequestException("Admins cannot be unbanned");
    }

    emailBanRepository.deleteByEmailAndTenant(
            user.getEmail(),
            user.getTenant()
    );

    user.setEnabled(true);

    auditService.log(
            admin.getEmail(),
            AuditAction.EMAIL_UNBANNED,
            "USER",
            user.getId(),
            null,
            null,
            admin.getTenant().getId()
    );
}
}



