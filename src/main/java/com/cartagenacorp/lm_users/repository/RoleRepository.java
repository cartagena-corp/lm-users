package com.cartagenacorp.lm_users.repository;

import com.cartagenacorp.lm_users.entity.Role;
import com.cartagenacorp.lm_users.entity.RoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, RoleId> {

    List<Role> findByOrganizationIdOrderByName(UUID organizationId);

    boolean existsByNameAndOrganizationId(String name, UUID organizationId);

    @Modifying
    @Query("DELETE FROM Role r WHERE r.organizationId = :organizationId")
    void deleteByOrganizationId(UUID organizationId);
}
