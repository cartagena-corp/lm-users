package com.cartagenacorp.lm_users.controller;

import com.cartagenacorp.lm_users.dto.NotificationResponse;
import com.cartagenacorp.lm_users.dto.RoleDto;
import com.cartagenacorp.lm_users.service.RoleService;
import com.cartagenacorp.lm_users.util.ConstantUtil;
import com.cartagenacorp.lm_users.util.RequiresPermission;
import com.cartagenacorp.lm_users.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{name}")
    public ResponseEntity<RoleDto> getRole(@PathVariable String name) {
        return ResponseEntity.ok(roleService.getRole(name));
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

    @GetMapping("/exists/{name}")
    @RequiresPermission({"ROLE_CRUD", "ROLE_READ"})
    public ResponseEntity<Boolean> roleExists(@PathVariable String name) {
        boolean exists = roleService.roleExists(name);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(exists);
    }
}
