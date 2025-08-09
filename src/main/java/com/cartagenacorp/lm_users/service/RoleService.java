package com.cartagenacorp.lm_users.service;

import com.cartagenacorp.lm_users.dto.RoleDto;
import com.cartagenacorp.lm_users.entity.Permission;
import com.cartagenacorp.lm_users.entity.Role;
import com.cartagenacorp.lm_users.exceptions.BaseException;
import com.cartagenacorp.lm_users.mapper.RoleMapper;
import com.cartagenacorp.lm_users.repository.PermissionRepository;
import com.cartagenacorp.lm_users.repository.RoleRepository;
import com.cartagenacorp.lm_users.util.ConstantUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

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
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(rol -> roleMapper.toDto(rol))
                .collect(Collectors.toList());
    }

    public RoleDto getRole(String name) {
        Role role =  roleRepository.findById(name)
                .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value()));

        return roleMapper.toDto(role);
    }

    public void deleteRole(String name) {
        Role role = roleRepository.findById(name)
                .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        roleRepository.delete(role);
    }

    public RoleDto createRole(RoleDto roleDto) {
        if (roleRepository.existsById(roleDto.getName())) {
            throw new BaseException(ConstantUtil.DUPLICATE_ROLE, HttpStatus.BAD_REQUEST.value());
        }
        Set<Permission> existingPermissions = roleDto.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getName())
                        .orElseThrow(() -> new BaseException(ConstantUtil.RESOURCE_NOT_FOUND + ": " + p.getName(), HttpStatus.NOT_FOUND.value())))
                .collect(Collectors.toSet());

        Role newRole = roleMapper.toEntity(roleDto);
        newRole.setPermissions(existingPermissions);
        Role savedRole = roleRepository.save(newRole);

        return roleMapper.toDto(savedRole);
    }

    public RoleDto updateRole(String name, RoleDto roleDto) {
        Role existingRole = roleRepository.findById(name)
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
        return roleRepository.existsById(name);
    }

}
