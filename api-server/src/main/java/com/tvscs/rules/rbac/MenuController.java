package com.tvscs.rules.rbac;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.AuthContext;
import com.tvscs.rules.auth.CurrentUser;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MenuController {
  private final MenuService menuService;

  public MenuController(MenuService menuService) {
    this.menuService = menuService;
  }

  @GetMapping("/menus")
  public List<MenuService.MenuView> menus() {
    CurrentUser user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    return menuService.getMenusForRoles(user.roleCodes());
  }
}
