package com.cartagenacorp.lm_users.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleId implements Serializable {
    private String name;
    private UUID organizationId;
}
