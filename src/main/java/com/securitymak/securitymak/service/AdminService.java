package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.UserAdminView;
import com.securitymak.securitymak.model.Role;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.RoleRepository;
import com.securitymak.securitymak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<UserAdminView> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserAdminView(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().getName()
                ))
                .toList();
    }

    public void updateUserRole(Long userId, String roleName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);
        userRepository.save(user);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
