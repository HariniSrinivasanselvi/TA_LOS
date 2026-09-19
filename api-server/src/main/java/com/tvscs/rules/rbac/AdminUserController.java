package com.tvscs.rules.rbac;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.AppUserEntity;
import com.tvscs.rules.auth.AppUserRepository;
import com.tvscs.rules.auth.AuthContext;
import com.tvscs.rules.auth.CurrentUser;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.*;

/** Admin-only screen for viewing every user and reassigning their Dealer/Admin role. */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
  private final AppUserRepository appUserRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final RoleService roleService;

  public AdminUserController(
      AppUserRepository appUserRepository,
      RoleRepository roleRepository,
      UserRoleRepository userRoleRepository,
      RoleService roleService) {
    this.appUserRepository = appUserRepository;
    this.roleRepository = roleRepository;
    this.userRoleRepository = userRoleRepository;
    this.roleService = roleService;
  }

  public record UserView(Long userId, String displayName, String email, String roleCode, String roleName) {}

  public record RoleChangeRequest(String roleCode) {}

  private CurrentUser requireAdmin() {
    CurrentUser user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    if (!user.isAdmin()) throw ApiException.forbidden("Only Admins can manage user roles.");
    return user;
  }

  @GetMapping
  public List<UserView> listUsers() {
    requireAdmin();
    Map<String, RoleEntity> rolesByCode =
        roleRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(RoleEntity::getRoleCode, r -> r));
    Map<Long, RoleEntity> rolesById = new java.util.HashMap<>();
    rolesByCode.values().forEach(r -> rolesById.put(r.getRoleId(), r));

    return appUserRepository.findAll().stream()
        .map(
            u -> {
              Set<String> codes = roleService.getRoleCodesForUser(u.getUserId());
              RoleEntity role = codes.stream().map(rolesByCode::get).filter(java.util.Objects::nonNull).findFirst().orElse(null);
              return new UserView(
                  u.getUserId(), u.displayName(), u.getEmail(), role != null ? role.getRoleCode() : null, role != null ? role.getRoleName() : "Unassigned");
            })
        .sorted(Comparator.comparing(UserView::displayName, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  @PostMapping("/{userId}/role")
  public UserView changeRole(@PathVariable Long userId, @RequestBody RoleChangeRequest body) {
    CurrentUser admin = requireAdmin();
    if (body == null || body.roleCode() == null || body.roleCode().isBlank()) {
      throw ApiException.badRequest("ROLE_CODE_REQUIRED", "roleCode is required.");
    }
    AppUserEntity target =
        appUserRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User not found."));

    boolean demotingSelfFromAdmin =
        userId.equals(admin.userId())
            && admin.isAdmin()
            && !RoleService.ADMIN_ROLE_CODE.equals(body.roleCode());
    if (demotingSelfFromAdmin && countAdmins() <= 1) {
      throw ApiException.badRequest(
          "LAST_ADMIN", "You are the only Admin; assign another Admin before changing your own role.");
    }

    roleService.setRole(userId, body.roleCode());
    RoleEntity role =
        roleRepository
            .findByRoleCode(body.roleCode())
            .orElseThrow(() -> ApiException.badRequest("UNKNOWN_ROLE", "Unknown role: " + body.roleCode()));
    return new UserView(target.getUserId(), target.displayName(), target.getEmail(), role.getRoleCode(), role.getRoleName());
  }

  private long countAdmins() {
    RoleEntity adminRole = roleRepository.findByRoleCode(RoleService.ADMIN_ROLE_CODE).orElse(null);
    if (adminRole == null) return 0;
    return appUserRepository.findAll().stream()
        .filter(u -> roleService.getRoleCodesForUser(u.getUserId()).contains(RoleService.ADMIN_ROLE_CODE))
        .count();
  }
}
