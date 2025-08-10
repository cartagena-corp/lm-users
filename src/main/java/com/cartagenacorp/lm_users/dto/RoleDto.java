package com.cartagenacorp.lm_users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for {@link com.cartagenacorp.lm_users.entity.Role}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto implements Serializable {
    private String name;
    private List<PermissionDto> permissions;
    private UUID organizationId;
}