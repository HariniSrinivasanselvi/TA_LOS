package com.tvscs.rules.rbac;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_role")
public class UserRoleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_role_id")
  private Long userRoleId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "role_id", nullable = false)
  private Long roleId;

  @Column(name = "created_date")
  private Instant createdDate = Instant.now();

  public UserRoleEntity() {}

  public UserRoleEntity(Long userId, Long roleId) {
    this.userId = userId;
    this.roleId = roleId;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getRoleId() {
    return roleId;
  }
}
