package org.example.tnal_youth_backend.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tnal_youth_backend.account.dto.RoleDto;
import org.example.tnal_youth_backend.account.entity.Role;
import org.example.tnal_youth_backend.account.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles API", description = "Manage organizational roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping
    public Role createRole(@RequestBody RoleDto roleDto) {
        return roleService.createRole(roleDto.getName(), roleDto.getDescription());
    }
}
