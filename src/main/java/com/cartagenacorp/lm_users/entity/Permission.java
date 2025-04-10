package com.cartagenacorp.lm_users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permission")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    @Id
    private String name; // e.g., ISSUE_CREATE, PROJECT_READ
}
