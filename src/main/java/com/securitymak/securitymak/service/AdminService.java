package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.UserAdminView;
import com.securitymak.securitymak.exception.BusinessRuleViolationException;
import com.securitymak.securitymak.exception.ResourceNotFoundException;
import com.securitymak.securitymak.model.Role;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.RoleRepository;
import com.securitymak.securitymak.repository.UserRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.securitymak.securitymak.dto.AuditLogView;
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

    public List<UserAdminView> getAllUsers() {

        Long tenantId = SecurityUtils.getCurrentTenantId();

        return userRepository.findAllByTenant_Id(tenantId)
                .stream()
                .map(user -> new UserAdminView(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().getName()
                ))
                .toList();
    }

    public Page<AuditLogView> getAuditLogs(
            String actorEmail,
            String action,
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


    public void updateUserRole(Long userId, String roleName) {

        Long tenantId = SecurityUtils.getCurrentTenantId();
        String actorEmail = SecurityUtils.getCurrentUserEmail();

        User user = userRepository
                .findByIdAndTenant_Id(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // prevent self-demotion
        if (user.getEmail().equals(actorEmail)) {
            throw new BusinessRuleViolationException("Admins cannot change their own role");
        }

        Role newRole = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new BusinessRuleViolationException("Role not found"));

        String oldRole = user.getRole().getName();
        if (oldRole.equals(newRole.getName())) {
            return;
        }

        user.setRole(newRole);
        userRepository.save(user);

        auditService.log(
                actorEmail,
                "CHANGE_USER_ROLE",
                "USER",
                user.getId(),
                oldRole,
                newRole.getName(),
                tenantId
        );
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}



