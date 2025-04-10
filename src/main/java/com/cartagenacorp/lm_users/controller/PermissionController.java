package com.cartagenacorp.lm_users.controller;

import com.cartagenacorp.lm_users.entity.Permission;
import com.cartagenacorp.lm_users.service.PermissionService;
import com.cartagenacorp.lm_users.util.RequiresPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @PostMapping
    @RequiresPermission({"ROLE_CRUD"})
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        return ResponseEntity.ok(permissionService.createPermission(permission));
    }

    @DeleteMapping("/{name}")
    @RequiresPermission({"ROLE_CRUD"})
    public ResponseEntity<?> deletePermission(@PathVariable String name) {
        try {
            permissionService.deletePermission(name);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (DataIntegrityViolationException ex){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The resource cannot be deleted because it is being used by other records");
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
