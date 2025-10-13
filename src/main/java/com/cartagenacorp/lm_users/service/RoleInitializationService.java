package com.cartagenacorp.lm_users.service;

import com.cartagenacorp.lm_users.entity.Permission;
import com.cartagenacorp.lm_users.entity.Role;
import com.cartagenacorp.lm_users.entity.RoleId;
import com.cartagenacorp.lm_users.repository.PermissionRepository;
import com.cartagenacorp.lm_users.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RoleInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(RoleInitializationService.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleInitializationService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public void createDefaultRoles(UUID organizationId) {
        logger.info("[RoleInitializationService] [createDefaultRoles] Iniciando creación de roles por defecto para la organización con ID={}", organizationId);

        RoleId adminRoleId = new RoleId("ADMIN", organizationId);

        if (roleRepository.existsById(adminRoleId)) {
            logger.info("[RoleInitializationService] [createDefaultRoles] El rol ADMIN ya existe para la organización con ID={}", organizationId);
            return;
        }

        logger.info("[RoleInitializationService] [createDefaultRoles] El rol ADMIN no existe. Creando rol por defecto.");

        List<Permission> allPermissions = permissionRepository.findAll();
        logger.debug("[RoleInitializationService] [createDefaultRoles] Se encontraron {} permisos en la base de datos", allPermissions.size());

        Set<Permission> adminPermissions = new HashSet<>();
        String[] adminPermissionNames = {
                "USER_CREATE", "USER_READ", "USER_UPDATE",
                "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE", "PERMISSION_READ",
                "CONFIG_PROJECT", "CONFIG_ISSUE", "CONFIG_SPRINT", "CONFIG_READ", "CONFIG_DELETE",
                "PROJECT_CREATE", "PROJECT_READ", "PROJECT_UPDATE", "PROJECT_DELETE",
                "ISSUE_CREATE", "ISSUE_READ", "ISSUE_UPDATE", "ISSUE_DELETE",
                "COMMENT_CREATE", "COMMENT_READ", "COMMENT_UPDATE", "COMMENT_DELETE",
                "SPRINT_CREATE", "SPRINT_READ", "SPRINT_UPDATE", "SPRINT_DELETE",
                "GEMINI_CONFIG", "GEMINI_ACTIVE", "IMPORT_PROJECT",
                "AUDIT_READ", "NOTIFICATION_CRUD", "NOTIFICATION_TYPE_CRUD"
        };

        for (String name : adminPermissionNames) {
            adminPermissions.add(getPermissionByName(allPermissions, name));
        }

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setOrganizationId(organizationId);
        adminRole.setPermissions(adminPermissions);
        roleRepository.save(adminRole);

        logger.info("[RoleInitializationService] [createDefaultRoles] Rol ADMIN creado exitosamente para la organización con  ID={}", organizationId);
    }

    private Permission getPermissionByName(List<Permission> permissions, String name) {
        return permissions.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("[RoleInitializationService] [getPermissionByName] No se encontró el permiso: {}", name);
                    return new IllegalArgumentException("Permiso no encontrado: " + name);
                });
    }
}
