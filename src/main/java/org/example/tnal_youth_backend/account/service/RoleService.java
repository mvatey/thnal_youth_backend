package org.example.tnal_youth_backend.account.service;

import org.example.tnal_youth_backend.account.entity.Role;
import org.example.tnal_youth_backend.account.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return roleRepository.save(role);
    }
}

