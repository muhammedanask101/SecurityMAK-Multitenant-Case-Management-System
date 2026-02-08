package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.UserAdminView;
import com.securitymak.securitymak.model.Role;
import com.securitymak.securitymak.service.AdminService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.securitymak.securitymak.dto.AuditLogView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserAdminView> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PutMapping("/users/{userId}/role")
    public String updateUserRole(
            @PathVariable Long userId,
            @RequestParam String roleName
    ) {
        adminService.updateUserRole(userId, roleName);
        return "Role updated successfully";
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return adminService.getAllRoles();
    }

    @GetMapping("/audit-logs")
    public Page<AuditLogView> getAuditLogs(
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            Pageable pageable
    ) {
        return adminService.getAuditLogs(
                actorEmail,
                action,
                targetType,
                from,
                to,
                pageable
        );
    }
}
