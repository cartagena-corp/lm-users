package com.cartagenacorp.lm_users.repository;

import com.cartagenacorp.lm_users.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    List<Permission> findAllByOrderByNameAsc();
}
