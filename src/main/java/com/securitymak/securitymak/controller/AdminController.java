package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.UserAdminView;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.Role;
import com.securitymak.securitymak.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.securitymak.securitymak.dto.AuditLogView;
import com.securitymak.securitymak.dto.UpdateClearanceRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
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
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("roleName is required");
        }
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
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            @PageableDefault(
                    sort = "timestamp",
                    direction = Sort.Direction.DESC
            )
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

    @PutMapping("/api/admin/users/{id}/clearance")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminView updateClearance(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClearanceRequest request
    ) {
        return adminService.updateUserClearance(id, request);
    }

    @PostMapping("/users/{id}/disable")
public void disableUser(@PathVariable Long id) {
    adminService.disableUser(id);
}

@PostMapping("/users/{id}/enable")
public void enableUser(@PathVariable Long id) {
    adminService.enableUser(id);
}
}
