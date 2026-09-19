package com.tvscs.rules.workflow;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.AuthContext;
import com.tvscs.rules.auth.CurrentUser;
import com.tvscs.rules.rbac.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
  private final ApplicationService applicationService;

  public ApplicationController(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  private CurrentUser requireDealer() {
    CurrentUser user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    if (!user.isDealer()) throw ApiException.forbidden("Only Dealers can submit trade advance applications.");
    return user;
  }

  private CurrentUser requireAuthenticated() {
    CurrentUser user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    return user;
  }

  @PostMapping
  public ApplicationView submit(@Valid @RequestBody ApplyRequest request) {
    return applicationService.submitApplication(requireDealer(), request);
  }

  @GetMapping("/mine")
  public List<ApplicationView> mine() {
    return applicationService.listMine(requireDealer());
  }

  @GetMapping("/{applicationId}")
  public ApplicationView get(@PathVariable Long applicationId) {
    return applicationService.getForCurrentUser(applicationId, requireAuthenticated());
  }
}
