package com.tvscs.rules.workflow;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.AuthContext;
import com.tvscs.rules.auth.CurrentUser;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/deviations")
public class AdminDeviationController {
  private final DeviationService deviationService;

  public AdminDeviationController(DeviationService deviationService) {
    this.deviationService = deviationService;
  }

  private CurrentUser requireAdmin() {
    CurrentUser user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    if (!user.isAdmin()) throw ApiException.forbidden("Only Admins can review deviations.");
    return user;
  }

  public record DecisionRequest(String remarks) {}

  @GetMapping
  public List<DeviationView> listPending() {
    requireAdmin();
    return deviationService.listPending();
  }

  @PostMapping("/{applicationId}/approve")
  public DeviationView approve(@PathVariable Long applicationId, @RequestBody(required = false) DecisionRequest body) {
    CurrentUser admin = requireAdmin();
    return deviationService.approve(applicationId, admin, body != null ? body.remarks() : null);
  }

  @PostMapping("/{applicationId}/reject")
  public DeviationView reject(@PathVariable Long applicationId, @RequestBody(required = false) DecisionRequest body) {
    CurrentUser admin = requireAdmin();
    return deviationService.reject(applicationId, admin, body != null ? body.remarks() : null);
  }
}
