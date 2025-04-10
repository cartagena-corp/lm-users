package com.cartagenacorp.lm_users.controller;

import com.cartagenacorp.lm_users.entity.Role;
import com.cartagenacorp.lm_users.service.RoleService;
import com.cartagenacorp.lm_users.util.RequiresPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getRole(@PathVariable String name) {
        try {
            return ResponseEntity.ok(roleService.getRole(name));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{name}")
    @RequiresPermission({"ROLE_CRUD"})
    public ResponseEntity<?> deleteRole(@PathVariable String name) {
        try {
            roleService.deleteRole(name);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @RequiresPermission({"ROLE_CRUD"})
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        try {
            return ResponseEntity.ok(roleService.createRole(role));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
        }
    }

    @PutMapping("/{name}")
    @RequiresPermission({"ROLE_CRUD"})
    public ResponseEntity<?> updateRole(@PathVariable String name, @RequestBody Role role) {
        try {
            return ResponseEntity.ok(roleService.updateRole(name, role));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
        }

    }
}
