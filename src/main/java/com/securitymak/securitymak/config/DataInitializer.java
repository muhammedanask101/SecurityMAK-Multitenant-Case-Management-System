package com.securitymak.securitymak.config;

import com.securitymak.securitymak.model.Role;
import com.securitymak.securitymak.model.Tenant;
import com.securitymak.securitymak.repository.RoleRepository;
import com.securitymak.securitymak.repository.TenantRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;

        @PostConstruct
        public void init() {

            if (roleRepository.count() == 0) {
                roleRepository.save(new Role("USER"));
                roleRepository.save(new Role("ADMIN"));
            }

            if (tenantRepository.count() == 0) {
                tenantRepository.save(
                    Tenant.builder()
                        .name("Default Organization")
                        .code("DEFAULT")
                        .build()
                );
            }
        }
}
