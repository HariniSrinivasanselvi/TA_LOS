package com.tvscs.rules.rbac;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotently ensures the "User management" menu entry exists and is mapped to the Admin role.
 * Runs on every startup (dev and prod) so new menu entries appear without a manual DB migration.
 */
@Component
public class RbacSeeder implements ApplicationRunner {
  private static final String USER_MANAGEMENT_MENU_CODE = "USER_MANAGEMENT";

  private final MenuRepository menuRepository;
  private final RoleRepository roleRepository;
  private final RoleMenuMappingRepository roleMenuMappingRepository;

  public RbacSeeder(
      MenuRepository menuRepository, RoleRepository roleRepository, RoleMenuMappingRepository roleMenuMappingRepository) {
    this.menuRepository = menuRepository;
    this.roleRepository = roleRepository;
    this.roleMenuMappingRepository = roleMenuMappingRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    MenuEntity menu =
        menuRepository.findByMenuCode(USER_MANAGEMENT_MENU_CODE)
            .orElseGet(
                () -> {
                  MenuEntity created = new MenuEntity();
                  created.setMenuCode(USER_MANAGEMENT_MENU_CODE);
                  created.setMenuName("User Management");
                  created.setDisplayName("User management");
                  created.setRoute("/user-management");
                  created.setActive(true);
                  created.setDisplayOrder(90);
                  return menuRepository.save(created);
                });

    roleRepository
        .findByRoleCode(RoleService.ADMIN_ROLE_CODE)
        .ifPresent(
            adminRole -> {
              boolean mapped =
                  roleMenuMappingRepository.findAllByRoleIdInAndActiveTrue(java.util.List.of(adminRole.getRoleId()))
                      .stream()
                      .anyMatch(m -> m.getMenuId().equals(menu.getMenuId()));
              if (!mapped) {
                RoleMenuMappingEntity mapping = new RoleMenuMappingEntity();
                mapping.setRoleId(adminRole.getRoleId());
                mapping.setMenuId(menu.getMenuId());
                mapping.setActive(true);
                roleMenuMappingRepository.save(mapping);
              }
            });
  }
}
