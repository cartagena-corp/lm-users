package com.cartagenacorp.lm_users.service;

import com.cartagenacorp.lm_users.dto.PermissionDto;
import com.cartagenacorp.lm_users.dto.RoleDto;
import com.cartagenacorp.lm_users.entity.Permission;
import com.cartagenacorp.lm_users.entity.Role;
import com.cartagenacorp.lm_users.entity.RoleId;
import com.cartagenacorp.lm_users.exceptions.BaseException;
import com.cartagenacorp.lm_users.mapper.RoleMapper;
import com.cartagenacorp.lm_users.repository.PermissionRepository;
import com.cartagenacorp.lm_users.repository.RoleRepository;
import com.cartagenacorp.lm_users.util.ConstantUtil;
import com.cartagenacorp.lm_users.util.JwtContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private static final Set<String> RESTRICTED_PERMISSIONS = Set.of(
            "ORGANIZATION_CONTROL",
            "PERMISSION_CREATE",
            "PERMISSION_DELETE"
    );

    private static final String superAdminRole = "SUPER_ADMIN";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository,
                       RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
    }

    public List<RoleDto> getAllRoles() {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        logger.info("[RoleService] [getAllRoles] Consultando todos los roles para la organización con ID={}", organizationId);

        List<Role> roles = roleRepository.findByOrganizationIdOrderByName(organizationId);
        logger.debug("[RoleService] [getAllRoles] Se encontraron {} roles", roles.size());

        return roles.stream()
                .map(this::toDtoWithSortedPermissions)
                .collect(Collectors.toList());
    }

    public RoleDto getRole(String name) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        logger.info("[RoleService] [getRole] Consultando rol '{}' para la organización con ID={}", name, organizationId);

        RoleId roleId = new RoleId(name, organizationId);

        Role role =  roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    logger.warn("[RoleService] [getRole] No se encontró el rol '{}' en la organización con ID={}", name, organizationId);
                    return new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
                });

        return roleMapper.toDto(role);
    }

    public RoleDto getRoleByOrganizationIdAndName(String name, UUID organizationId) {
        logger.info("[RoleService] [getRoleByOrganizationIdAndName] Consultando rol '{}' en la organización con ID={}", name, organizationId);

        RoleId roleId = new RoleId(name, organizationId);

        Role role =  roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    logger.warn("[RoleService] [getRoleByOrganizationIdAndName] No se encontró el rol '{}' en la organización con ID={}", name, organizationId);
                    return new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
                });

        return roleMapper.toDto(role);
    }

    public void deleteRole(String name) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        logger.info("[RoleService] [deleteRole] Eliminando rol '{}' para la organización con ID={}", name, organizationId);

        RoleId roleId = new RoleId(name, organizationId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    logger.warn("[RoleService] [deleteRole] No se encontró el rol '{}' para eliminar en la organización con ID={}", name, organizationId);
                    return new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
                });
        roleRepository.delete(role);
        logger.info("[RoleService] [deleteRole] Rol '{}' eliminado exitosamente para la organización con ID={}", name, organizationId);
    }

    public RoleDto createRole(RoleDto roleDto) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        logger.info("[RoleService] [createRole] Creando rol '{}' para la organización con ID={}", roleDto.getName(), organizationId);

        if (superAdminRole.equalsIgnoreCase(roleDto.getName())
                && !superAdminRole.equals(JwtContextHolder.getRole())) {
            logger.warn("[RoleService] [createRole] Un usuario sin rol SUPER_ADMIN intentó crear un rol SUPER_ADMIN");
            throw new BaseException(ConstantUtil.EXCLUSIVE_SUPER_ADMIN_ROL, HttpStatus.FORBIDDEN.value());
        }

        if (!superAdminRole.equals(JwtContextHolder.getRole())) {
            logger.debug("[RoleService] [createRole] Validando permisos del nuevo rol '{}'", roleDto.getName());
            validatePermissionsForNonSuperAdmin(roleDto.getPermissions().stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toSet()));
        }

        if (roleRepository.existsByNameAndOrganizationId(roleDto.getName(), organizationId)) {
            logger.warn("[RoleService] [createRole] El rol '{}' ya existe en la organización con ID={}", roleDto.getName(), organizationId);
            throw new BaseException(ConstantUtil.DUPLICATE_ROLE, HttpStatus.BAD_REQUEST.value());
        }

        Set<Permission> existingPermissions = roleDto.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getName())
                        .orElseThrow(() -> {
                            logger.error("[RoleService] [createRole] No se encontró el permiso '{}'", p.getName());
                            return new BaseException(ConstantUtil.RESOURCE_NOT_FOUND + ": " + p.getName(), HttpStatus.NOT_FOUND.value());
                        }))
                .collect(Collectors.toSet());

        Role newRole = roleMapper.toEntity(roleDto);
        newRole.setOrganizationId(organizationId);
        newRole.setPermissions(existingPermissions);

        Role savedRole = roleRepository.save(newRole);
        logger.info("[RoleService] [createRole] Rol '{}' creado exitosamente para la organización con ID={}", savedRole.getName(), organizationId);

        return roleMapper.toDto(savedRole);
    }

    public RoleDto updateRole(String name, RoleDto roleDto) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        logger.info("[RoleService] [updateRole] Actualizando rol '{}' para la organización con ID={}", name, organizationId);

        RoleId roleId = new RoleId(name, organizationId);

        if (!superAdminRole.equals(JwtContextHolder.getRole())) {
            logger.debug("[RoleService] [updateRole] Validando permisos actualizados para el rol '{}'", name);
            validatePermissionsForNonSuperAdmin(roleDto.getPermissions().stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toSet()));
        }

        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    logger.warn("[RoleService] [updateRole] Rol '{}' no encontrado en la organización con ID={}", name, organizationId);
                    return new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
                });

        roleDto.setName(name);
        roleMapper.partialUpdate(roleDto, existingRole);

        Set<Permission> existingPermissions = roleDto.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getName())
                        .orElseThrow(() -> {
                            logger.error("[RoleService] [updateRole] No se encontró el permiso '{}'", p.getName());
                            return new BaseException(ConstantUtil.RESOURCE_NOT_FOUND + ": " + p.getName(), HttpStatus.NOT_FOUND.value());
                        }))
                .collect(Collectors.toSet());

        existingRole.setPermissions(existingPermissions);
        Role savedRole = roleRepository.save(existingRole);
        logger.info("[RoleService] [updateRole] Rol '{}' actualizado exitosamente en la organización con ID={}", name, organizationId);

        return roleMapper.toDto(savedRole);
    }

    public boolean roleExists(String name) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        logger.info("[RoleService] [roleExists] Verificando si existe el rol {} para la organización con ID={}", name, organizationId);
        return roleRepository.existsByNameAndOrganizationId(name, organizationId);
    }

    @Transactional
    public void deleteByOrganizationId(UUID organizationId) {
        logger.info("[RoleService] [deleteByOrganizationId] Eliminando roles de la organización con ID={}", organizationId);
        roleRepository.deleteByOrganizationId(organizationId);
    }

    public List<RoleDto> getAllRolesByOrganizationId(UUID organizationId) {
        logger.info("[RoleService] [getAllRolesByOrganizationId] Consultando roles para la organización con ID={}", organizationId);

        List<Role> roles = roleRepository.findByOrganizationIdOrderByName(organizationId);
        logger.debug("[RoleService] [getAllRolesByOrganizationId] Se encontraron {} roles en la organización con ID={}", roles.size(), organizationId);

        List<RoleDto> result = roles.stream()
                .map(this::toDtoWithSortedPermissions)
                .collect(Collectors.toList());

        logger.info("[RoleService] [getAllRolesByOrganizationId] Retornando {} roles para la organización con ID={}", result.size(), organizationId);
        return result;
    }

    private RoleDto toDtoWithSortedPermissions(Role role) {
        List<PermissionDto> sortedPermissions = role.getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getName))
                .map(this::toPermissionDto)
                .collect(Collectors.toList());

        RoleDto roleDto = new RoleDto();
        roleDto.setName(role.getName());
        roleDto.setOrganizationId(role.getOrganizationId());
        roleDto.setPermissions(sortedPermissions);
        return roleDto;
    }

    private PermissionDto toPermissionDto(Permission permission) {
        PermissionDto permissionDTO = new PermissionDto();
        permissionDTO.setName(permission.getName());
        return permissionDTO;
    }

    private void validatePermissionsForNonSuperAdmin(Set<String> assignedPermissions) {
        logger.info("[RoleService] [validatePermissionsForNonSuperAdmin] Validando {} permisos para usuario no super admin", assignedPermissions.size());
        if (assignedPermissions.stream().anyMatch(RESTRICTED_PERMISSIONS::contains)) {
            logger.warn("[RoleService] [validatePermissionsForNonSuperAdmin] Se detectaron permisos restringidos en la asignación: {}", assignedPermissions);
            throw new BaseException("No tiene permisos para asignar uno o más de estos roles", HttpStatus.FORBIDDEN.value());
        }
    }
}
