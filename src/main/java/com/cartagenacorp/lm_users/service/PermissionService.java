package com.cartagenacorp.lm_users.service;

import com.cartagenacorp.lm_users.dto.PermissionDto;
import com.cartagenacorp.lm_users.entity.Permission;
import com.cartagenacorp.lm_users.exceptions.BaseException;
import com.cartagenacorp.lm_users.mapper.PermissionMapper;
import com.cartagenacorp.lm_users.repository.PermissionRepository;
import com.cartagenacorp.lm_users.util.ConstantUtil;
import com.cartagenacorp.lm_users.util.JwtContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    public List<PermissionDto> getAllPermissions() {
        logger.info("[PermissionService] [getAllPermissions] Consultando todos los permisos en la base de datos");

        List<Permission> permissions = permissionRepository.findAllByOrderByNameAsc();
        logger.debug("[PermissionService] [getAllPermissions] Se encontraron {} permisos", permissions.size());

        Set<String> excludedPermissions = Set.of(
                "ORGANIZATION_CONTROL",
                "PERMISSION_CREATE",
                "PERMISSION_DELETE"
        );
        String superAdminRole = "SUPER_ADMIN";
        String currentRole = JwtContextHolder.getRole();

        if (superAdminRole.equals(currentRole)) {
            logger.info("[PermissionService] [getAllPermissions] Usuario con rol SUPER_ADMIN, retornando todos los permisos");
            return permissions.stream()
                    .map(permission -> permissionMapper.toDto(permission))
                    .collect(Collectors.toList());
        } else {
            logger.info("[PermissionService] [getAllPermissions] Usuario con rol {}, excluyendo permisos restringidos", currentRole);
            return permissions.stream()
                    .filter(permission -> !excludedPermissions.contains(permission.getName()))
                    .map(permission -> permissionMapper.toDto(permission))
                    .collect(Collectors.toList());
        }
    }

    public PermissionDto createPermission(PermissionDto permissionDto) {
        logger.info("[PermissionService] [createPermission] Creando nuevo permiso con nombre {}", permissionDto.getName());

        if (permissionRepository.existsById(permissionDto.getName())) {
            logger.warn("[PermissionService] [createPermission] El permiso {} ya existe", permissionDto.getName());
            throw new BaseException(ConstantUtil.DUPLICATE_PERMISSION, HttpStatus.BAD_REQUEST.value());
        }
        Permission newPermission = permissionMapper.toEntity(permissionDto);
        Permission savedPermission = permissionRepository.save(newPermission);

        logger.info("[PermissionService] [createPermission] Permiso {} creado exitosamente", savedPermission.getName());
        return permissionMapper.toDto(savedPermission);
    }

    public void deletePermission(String name) {
        logger.info("[PermissionService] [deletePermission] Eliminando permiso con nombre {}", name);

        Permission permission = permissionRepository.findById(name)
                .orElseThrow(() -> {
                    logger.warn("[PermissionService] [deletePermission] No se encontró el permiso {}", name);
                    return new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
                });

        try {
            permissionRepository.delete(permission);
        } catch (Exception e) {
            logger.error("[PermissionService] [deletePermission] Error al eliminar el permiso {}: {}", name, e.getMessage());
            throw new BaseException(ConstantUtil.PERMISSION_IN_USE, HttpStatus.BAD_REQUEST.value());
        }

        logger.info("[PermissionService] [deletePermission] Permiso {} eliminado exitosamente", name);
    }
}
