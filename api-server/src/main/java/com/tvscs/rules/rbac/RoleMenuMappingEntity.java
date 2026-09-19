package com.tvscs.rules.rbac;

import jakarta.persistence.*;

@Entity
@Table(name = "role_menu_mapping")
public class RoleMenuMappingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_menu_id")
  private Long roleMenuId;

  @Column(name = "role_id", nullable = false)
  private Long roleId;

  @Column(name = "menu_id", nullable = false)
  private Long menuId;

  private boolean active = true;

  public Long getRoleId() {
    return roleId;
  }

  public Long getMenuId() {
    return menuId;
  }

  public boolean isActive() {
    return active;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public void setMenuId(Long menuId) {
    this.menuId = menuId;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
