package com.tvscs.rules.rbac;

import jakarta.persistence.*;

@Entity
@Table(name = "menu")
public class MenuEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "menu_id")
  private Long menuId;

  @Column(name = "menu_code", unique = true, nullable = false)
  private String menuCode;

  @Column(name = "menu_name", nullable = false)
  private String menuName;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(name = "route", nullable = false)
  private String route;

  private boolean active = true;

  @Column(name = "display_order")
  private int displayOrder;

  public Long getMenuId() {
    return menuId;
  }

  public String getMenuCode() {
    return menuCode;
  }

  public String getMenuName() {
    return menuName;
  }

  public void setMenuName(String menuName) {
    this.menuName = menuName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getRoute() {
    return route;
  }

  public boolean isActive() {
    return active;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public void setMenuCode(String menuCode) {
    this.menuCode = menuCode;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }
}
