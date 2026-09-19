package com.tvscs.rules.rbac;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class RoleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private Long roleId;

  @Column(name = "role_code", unique = true, nullable = false)
  private String roleCode;

  @Column(name = "role_name", nullable = false)
  private String roleName;

  private boolean active = true;

  public Long getRoleId() {
    return roleId;
  }

  public String getRoleCode() {
    return roleCode;
  }

  public String getRoleName() {
    return roleName;
  }

  public boolean isActive() {
    return active;
  }
}
