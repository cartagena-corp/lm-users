package com.cartagenacorp.lm_users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.cartagenacorp.lm_users.entity.Permission}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDto implements Serializable {
    private String name;
}