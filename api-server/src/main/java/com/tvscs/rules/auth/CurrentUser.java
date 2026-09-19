package com.tvscs.rules.auth;

import java.util.Set;

public record CurrentUser(Long userId, String subject, String email, String displayName, Set<String> roleCodes) {
  public boolean hasRole(String roleCode) {
    return roleCodes.contains(roleCode);
  }

  public boolean isAdmin() {
    return hasRole(com.tvscs.rules.rbac.RoleService.ADMIN_ROLE_CODE);
  }

  public boolean isDealer() {
    return hasRole(com.tvscs.rules.rbac.RoleService.DEALER_ROLE_CODE);
  }
}
