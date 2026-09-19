package com.tvscs.rules.workflow;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.AuthContext;
import com.tvscs.rules.auth.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications/{applicationId}/vkyc")
public class VkycController {
  private final VkycService vkycService;

  public VkycController(VkycService vkycService) {
    this.vkycService = vkycService;
  }

  private CurrentUser requireDealer() {
    CurrentUser user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    if (!user.isDealer()) throw ApiException.forbidden("Only Dealers can submit VKYC.");
    return user;
  }

  private CurrentUser requireAuthenticated() {
    CurrentUser user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    return user;
  }

  @PostMapping
  public VkycView submit(@PathVariable Long applicationId, @Valid @RequestBody VkycRequest request) {
    return vkycService.submitVkyc(applicationId, requireDealer(), request);
  }

  @GetMapping
  public VkycView get(@PathVariable Long applicationId) {
    return vkycService.getForCurrentUser(applicationId, requireAuthenticated());
  }
}
