package com.cartagenacorp.lm_users.controller;

import com.cartagenacorp.lm_users.dto.NotificationResponse;
import com.cartagenacorp.lm_users.dto.PermissionDto;
import com.cartagenacorp.lm_users.service.PermissionService;
import com.cartagenacorp.lm_users.util.ConstantUtil;
import com.cartagenacorp.lm_users.util.RequiresPermission;
import com.cartagenacorp.lm_users.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @RequiresPermission({"PERMISSION_CRUD", "PERMISSION_READ"})
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @PostMapping
    @RequiresPermission({"PERMISSION_CRUD", "PERMISSION_CREATE"})
    public ResponseEntity<PermissionDto> createPermission(@RequestBody PermissionDto permissionDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(permissionService.createPermission(permissionDto));
    }

    @DeleteMapping("/{name}")
    @RequiresPermission({"PERMISSION_CRUD", "PERMISSION_DELETE"})
    public ResponseEntity<NotificationResponse> deletePermission(@PathVariable String name) {
        permissionService.deletePermission(name);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(ConstantUtil.Success.PERMISSION_DELETED, HttpStatus.OK));
    }
}
