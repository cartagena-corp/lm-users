package com.cartagenacorp.lm_users.controller;

import com.cartagenacorp.lm_users.dto.NotificationResponse;
import com.cartagenacorp.lm_users.dto.RoleDto;
import com.cartagenacorp.lm_users.service.RoleInitializationService;
import com.cartagenacorp.lm_users.service.RoleService;
import com.cartagenacorp.lm_users.util.ConstantUtil;
import com.cartagenacorp.lm_users.util.RequiresPermission;
import com.cartagenacorp.lm_users.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final RoleInitializationService roleInitializationService;

    public RoleController(RoleService roleService, RoleInitializationService roleInitializationService) {
        this.roleService = roleService;
        this.roleInitializationService = roleInitializationService;
    }

    @GetMapping
    @RequiresPermission({"ROLE_CRUD", "ROLE_READ"})
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{name}")
    @RequiresPermission({"ROLE_CRUD", "ROLE_READ"})
    public ResponseEntity<RoleDto> getRole(@PathVariable String name) {
        return ResponseEntity.ok(roleService.getRole(name));
    }

    @GetMapping("/{organizationId}/{name}") //se usa desde lm-oauth (uso interno)
    public ResponseEntity<RoleDto> getRoleByOrganizationIdAndName(
            @PathVariable UUID organizationId,
            @PathVariable String name) {
        return ResponseEntity.ok(roleService.getRoleByOrganizationIdAndName(name, organizationId));
    }

    @DeleteMapping("/{name}")
    @RequiresPermission({"ROLE_CRUD", "ROLE_DELETE"})
    public ResponseEntity<NotificationResponse> deleteRole(@PathVariable String name) {
        roleService.deleteRole(name);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(ConstantUtil.Success.ROLE_DELETED, HttpStatus.OK));
    }

    @PostMapping
    @RequiresPermission({"ROLE_CRUD", "ROLE_CREATE"})
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto roleDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.createRole(roleDto));
    }

    @PutMapping("/{name}")
    @RequiresPermission({"ROLE_CRUD", "ROLE_UPDATE"})
    public ResponseEntity<RoleDto> updateRole(@PathVariable String name, @RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(roleService.updateRole(name, roleDto));
    }

    @GetMapping("/exists/{name}") //se usa desde lm-oauth (uso interno)
    @RequiresPermission({"USER_UPDATE"})
    public ResponseEntity<Boolean> roleExists(@PathVariable String name) {
        boolean exists = roleService.roleExists(name);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(exists);
    }

    @PostMapping("/initialize/{organizationId}") //se usa desde lm-organization (uso interno)
    @RequiresPermission({"ORGANIZATION_CONTROL", "ORGANIZATION_CREATE"})
    public ResponseEntity<Void> initializeRolesForOrganization(@PathVariable UUID organizationId) {
        roleInitializationService.createDefaultRoles(organizationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/organization/{organizationId}") //se usa desde lm-organization (uso interno)
    @RequiresPermission({"ORGANIZATION_CONTROL", "ORGANIZATION_DELETE"})
    public ResponseEntity<Void> deleteRolesByOrganizationId(@PathVariable UUID organizationId) {
        roleService.deleteByOrganizationId(organizationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/organization/{organizationId}/roles")
    @RequiresPermission({"ORGANIZATION_CONTROL", "ORGANIZATION_READ"})
    public ResponseEntity<List<RoleDto>> getAllRolesByOrganizationId(@PathVariable String organizationId) {
        UUID organizationUUID = UUID.fromString(organizationId);
        return ResponseEntity.ok(roleService.getAllRolesByOrganizationId(organizationUUID));
    }
}
