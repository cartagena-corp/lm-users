package com.cartagenacorp.lm_users.service;

import com.cartagenacorp.lm_users.entity.Permission;
import com.cartagenacorp.lm_users.entity.Role;
import com.cartagenacorp.lm_users.repository.PermissionRepository;
import com.cartagenacorp.lm_users.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRole(String name) {
        return roleRepository.findById(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
    }

    public void deleteRole(String name) {
        Role role = roleRepository.findById(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        roleRepository.delete(role);
    }

    public Role createRole(Role role) {
        if (roleRepository.existsById(role.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The role already exists: " + role.getName());
        }
        Set<Permission> existingPermissions = role.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getName())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found: " + p.getName())))
                .collect(Collectors.toSet());

        role.setPermissions(existingPermissions);
        return roleRepository.save(role);
    }

    public Role updateRole(String name, Role updated) {
        Role role = roleRepository.findById(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        Set<Permission> existingPermissions = updated.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getName())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found: " + p.getName())))
                .collect(Collectors.toSet());

        role.setPermissions(existingPermissions);
        return roleRepository.save(role);
    }

}
