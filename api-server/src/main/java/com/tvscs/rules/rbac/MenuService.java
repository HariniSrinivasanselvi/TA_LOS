package com.tvscs.rules.rbac;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MenuService {
  private final RoleRepository roleRepository;
  private final RoleMenuMappingRepository mappingRepository;
  private final MenuRepository menuRepository;

  public MenuService(
      RoleRepository roleRepository, RoleMenuMappingRepository mappingRepository, MenuRepository menuRepository) {
    this.roleRepository = roleRepository;
    this.mappingRepository = mappingRepository;
    this.menuRepository = menuRepository;
  }

  public record MenuView(String code, String displayName, String route) {}

  public List<MenuView> getMenusForRoles(Set<String> roleCodes) {
    List<Long> roleIds =
        roleRepository.findAll().stream()
            .filter(r -> roleCodes.contains(r.getRoleCode()))
            .map(RoleEntity::getRoleId)
            .toList();
    if (roleIds.isEmpty()) return List.of();
    List<Long> menuIds = mappingRepository.findAllByRoleIdInAndActiveTrue(roleIds).stream()
        .map(RoleMenuMappingEntity::getMenuId)
        .distinct()
        .toList();
    return menuRepository.findAllByMenuIdIn(menuIds).stream()
        .filter(MenuEntity::isActive)
        .sorted(Comparator.comparingInt(MenuEntity::getDisplayOrder))
        .map(m -> new MenuView(m.getMenuCode(), m.getDisplayName(), m.getRoute()))
        .toList();
  }
}
