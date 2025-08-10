package com.cartagenacorp.lm_users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "role")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(RoleId.class)
public class Role {
    @Id
    @Column(name = "name")
    private String name; // ADMIN, DEVELOPER, CONSULTANT

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission",
            joinColumns = {
                    @JoinColumn(name = "role_name", referencedColumnName = "name"),
                    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id")
            },
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    private Set<Permission> permissions = new HashSet<>();
}
