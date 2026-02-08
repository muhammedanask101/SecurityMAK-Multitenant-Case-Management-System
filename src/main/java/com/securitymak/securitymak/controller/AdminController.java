package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.UserAdminView;
import com.securitymak.securitymak.model.Role;
import com.securitymak.securitymak.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
