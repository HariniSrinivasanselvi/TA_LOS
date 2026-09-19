package com.tvscs.rules.rbac;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bootstraps and resolves Dealer (DLR0001) / Admin (AD0001) role assignments. */
@Service
public class RoleService {
  public static final String DEALER_ROLE_CODE = "DLR0001";
  public static final String ADMIN_ROLE_CODE = "AD0001";

  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;

  public RoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
    this.roleRepository = roleRepository;
    this.userRoleRepository = userRoleRepository;
  }

  /**
   * Assigns a role to a brand-new user. The very first user to ever log in becomes Admin
   * (bootstrap operator); every subsequent new user defaults to Dealer. Admins can change this
   * afterwards from the user management screen.
   */
  @Transactional
  public void assignDefaultRoleForNewUser(Long userId) {
    boolean isFirstUser = userRoleRepository.count() == 0;
    String roleCode = isFirstUser ? ADMIN_ROLE_CODE : DEALER_ROLE_CODE;
    RoleEntity role =
        roleRepository
            .findByRoleCode(roleCode)
            .orElseThrow(() -> new IllegalStateException("Role not seeded: " + roleCode));
    userRoleRepository.save(new UserRoleEntity(userId, role.getRoleId()));
  }

  public Set<String> getRoleCodesForUser(Long userId) {
    List<Long> roleIds = userRoleRepository.findAllByUserId(userId).stream().map(UserRoleEntity::getRoleId).toList();
    if (roleIds.isEmpty()) return Set.of();
    return roleRepository.findAllById(roleIds).stream().map(RoleEntity::getRoleCode).collect(Collectors.toSet());
  }

  @Transactional
  public void setRole(Long userId, String roleCode) {
    RoleEntity role =
        roleRepository.findByRoleCode(roleCode).orElseThrow(() -> new IllegalArgumentException("Unknown role: " + roleCode));
    userRoleRepository.findAllByUserId(userId).forEach(userRoleRepository::delete);
    userRoleRepository.save(new UserRoleEntity(userId, role.getRoleId()));
  }
}
