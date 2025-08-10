package com.cartagenacorp.lm_users.service;

import com.cartagenacorp.lm_users.entity.Permission;
import com.cartagenacorp.lm_users.entity.Role;
import com.cartagenacorp.lm_users.entity.RoleId;
import com.cartagenacorp.lm_users.repository.PermissionRepository;
import com.cartagenacorp.lm_users.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RoleInitializationService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleInitializationService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public void createDefaultRoles(UUID organizationId) {
        if (!roleRepository.existsById(new RoleId("ADMIN", organizationId))) {
            List<Permission> allPermissions = permissionRepository.findAll();

            Set<Permission> adminPermissions = new HashSet<>();
            adminPermissions.add(getPermissionByName(allPermissions, "USER_CREATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "USER_READ"));
            adminPermissions.add(getPermissionByName(allPermissions, "USER_UPDATE"));

            adminPermissions.add(getPermissionByName(allPermissions, "ROLE_CREATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "ROLE_READ"));
            adminPermissions.add(getPermissionByName(allPermissions, "ROLE_UPDATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "ROLE_DELETE"));
            adminPermissions.add(getPermissionByName(allPermissions, "PERMISSION_READ"));

            adminPermissions.add(getPermissionByName(allPermissions, "CONFIG_PROJECT"));
            adminPermissions.add(getPermissionByName(allPermissions, "CONFIG_ISSUE"));
            adminPermissions.add(getPermissionByName(allPermissions, "CONFIG_SPRINT"));

            adminPermissions.add(getPermissionByName(allPermissions, "PROJECT_CREATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "PROJECT_READ"));
            adminPermissions.add(getPermissionByName(allPermissions, "PROJECT_UPDATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "PROJECT_DELETE"));

            adminPermissions.add(getPermissionByName(allPermissions, "ISSUE_CREATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "ISSUE_READ"));
            adminPermissions.add(getPermissionByName(allPermissions, "ISSUE_UPDATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "ISSUE_DELETE"));

            adminPermissions.add(getPermissionByName(allPermissions, "COMMENT_CREATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "COMMENT_READ"));
            adminPermissions.add(getPermissionByName(allPermissions, "COMMENT_UPDATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "COMMENT_DELETE"));

            adminPermissions.add(getPermissionByName(allPermissions, "SPRINT_CREATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "SPRINT_READ"));
            adminPermissions.add(getPermissionByName(allPermissions, "SPRINT_UPDATE"));
            adminPermissions.add(getPermissionByName(allPermissions, "SPRINT_DELETE"));

            adminPermissions.add(getPermissionByName(allPermissions, "GEMINI_CONFIG"));
            adminPermissions.add(getPermissionByName(allPermissions, "GEMINI_ACTIVE"));
            adminPermissions.add(getPermissionByName(allPermissions, "IMPORT_PROJECT"));

            adminPermissions.add(getPermissionByName(allPermissions, "AUDIT_READ"));
            adminPermissions.add(getPermissionByName(allPermissions, "NOTIFICATION_CRUD"));
            adminPermissions.add(getPermissionByName(allPermissions, "NOTIFICATION_TYPE_CRUD"));

            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setOrganizationId(organizationId);
            adminRole.setPermissions(adminPermissions);
            roleRepository.save(adminRole);
        }
    }

    private Permission getPermissionByName(List<Permission> permissions, String name) {
        return permissions.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + name));
    }
}
