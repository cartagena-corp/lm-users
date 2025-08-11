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
        List<Role> roles = roleRepository.findByOrganizationIdOrderByName(organizationId);

        return roles.stream()
                .map(this::toDtoWithSortedPermissions)
                .collect(Collectors.toList());
    }

    public RoleDto getRole(String name) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        RoleId roleId = new RoleId(name, organizationId);

        Role role =  roleRepository.findById(roleId)
                .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value()));

        return roleMapper.toDto(role);
    }

    public RoleDto getRoleByOrganizationIdAndName(String name, UUID organizationId) {
        RoleId roleId = new RoleId(name, organizationId);

        Role role =  roleRepository.findById(roleId)
                .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value()));

        return roleMapper.toDto(role);
    }

    public void deleteRole(String name) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        RoleId roleId = new RoleId(name, organizationId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        roleRepository.delete(role);
    }

    public RoleDto createRole(RoleDto roleDto) {
        UUID organizationId = JwtContextHolder.getOrganizationId();

        if (!superAdminRole.equals(JwtContextHolder.getRole())) {
            validatePermissionsForNonSuperAdmin(roleDto.getPermissions().stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toSet()));
        }

        if (roleRepository.existsByNameAndOrganizationId(roleDto.getName(), organizationId)) {
            throw new BaseException(ConstantUtil.DUPLICATE_ROLE, HttpStatus.BAD_REQUEST.value());
        }
        Set<Permission> existingPermissions = roleDto.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getName())
                        .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND + ": " + p.getName(), HttpStatus.NOT_FOUND.value())))
                .collect(Collectors.toSet());

        Role newRole = roleMapper.toEntity(roleDto);
        newRole.setOrganizationId(organizationId);
        newRole.setPermissions(existingPermissions);
        Role savedRole = roleRepository.save(newRole);

        return roleMapper.toDto(savedRole);
    }

    public RoleDto updateRole(String name, RoleDto roleDto) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        RoleId roleId = new RoleId(name, organizationId);

        if (!superAdminRole.equals(JwtContextHolder.getRole())) {
            validatePermissionsForNonSuperAdmin(roleDto.getPermissions().stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toSet()));
        }

        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value()));

        roleMapper.partialUpdate(roleDto, existingRole);

        Set<Permission> existingPermissions = roleDto.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getName())
                        .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND + ": " + p.getName(), HttpStatus.NOT_FOUND.value())))
                .collect(Collectors.toSet());

        existingRole.setPermissions(existingPermissions);
        Role savedRole = roleRepository.save(existingRole);

        return roleMapper.toDto(savedRole);
    }

    public boolean roleExists(String name) {
        UUID organizationId = JwtContextHolder.getOrganizationId();
        return roleRepository.existsByNameAndOrganizationId(name, organizationId);
    }

    @Transactional
    public void deleteByOrganizationId(UUID organizationId) {
        roleRepository.deleteByOrganizationId(organizationId);
    }

    public List<RoleDto> getAllRolesByOrganizationId(UUID organizationId) {
        List<Role> roles = roleRepository.findByOrganizationIdOrderByName(organizationId);

        return roles.stream()
                .map(this::toDtoWithSortedPermissions)
                .collect(Collectors.toList());
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
        if (assignedPermissions.stream().anyMatch(RESTRICTED_PERMISSIONS::contains)) {
            throw new BaseException("No tiene permisos para asignar uno o más de estos roles", HttpStatus.FORBIDDEN.value());
        }
    }

}
