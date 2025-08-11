package com.cartagenacorp.lm_users.service;

import com.cartagenacorp.lm_users.dto.PermissionDto;
import com.cartagenacorp.lm_users.entity.Permission;
import com.cartagenacorp.lm_users.exceptions.BaseException;
import com.cartagenacorp.lm_users.mapper.PermissionMapper;
import com.cartagenacorp.lm_users.repository.PermissionRepository;
import com.cartagenacorp.lm_users.util.ConstantUtil;
import com.cartagenacorp.lm_users.util.JwtContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    public List<PermissionDto> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAllByOrderByNameAsc();
        Set<String> excludedPermissions = Set.of(
                "ORGANIZATION_CONTROL",
                "PERMISSION_CREATE",
                "PERMISSION_DELETE"
        );
        String superAdminRole = "SUPER_ADMIN";

        if (superAdminRole.equals(JwtContextHolder.getRole())) {
            return permissions.stream()
                    .map(permission -> permissionMapper.toDto(permission))
                    .collect(Collectors.toList());
        } else {
            return permissions.stream()
                    .filter(permission -> !excludedPermissions.contains(permission.getName()))
                    .map(permission -> permissionMapper.toDto(permission))
                    .collect(Collectors.toList());
        }
    }

    public PermissionDto createPermission(PermissionDto permissionDto) {
        if (permissionRepository.existsById(permissionDto.getName())) {
            throw new BaseException(ConstantUtil.DUPLICATE_PERMISSION, HttpStatus.BAD_REQUEST.value());
        }
        Permission newPermission = permissionMapper.toEntity(permissionDto);
        Permission savedPermission = permissionRepository.save(newPermission);

        return permissionMapper.toDto(savedPermission);
    }

    public void deletePermission(String name) {
        Permission permission = permissionRepository.findById(name)
                .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        permissionRepository.delete(permission);
    }
}
