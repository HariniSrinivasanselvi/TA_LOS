package com.tvscs.rules.api;

import static com.tvscs.rules.model.RuleModels.*;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.AuthContext;
import com.tvscs.rules.service.GoRulesDecisionService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/** Rule Configuration / Eligibility Check tooling. Admin-only, except the public health check. */
@RestController
@RequestMapping("/api")
public class RuleEngineController {
  private final GoRulesDecisionService decisionService;

  public RuleEngineController(GoRulesDecisionService decisionService) {
    this.decisionService = decisionService;
  }

  private void requireAdmin() {
    var user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    if (!user.isAdmin()) throw ApiException.forbidden("Only Admins can access Rule Configuration / Eligibility Check tools.");
  }

  @GetMapping("/healthz")
  public Map<String, String> health() {
    return Map.of("status", "ok", "engine", "GoRules-compatible JDM");
  }

  @GetMapping("/rules/config")
  public RuleConfiguration getConfiguration() {
    requireAdmin();
    return decisionService.getConfiguration();
  }

  @PutMapping("/rules/config")
  public RuleConfiguration updateConfiguration(@Valid @RequestBody RuleConfigurationUpdate update) {
    requireAdmin();
    return decisionService.updateConfiguration(update);
  }

  @PostMapping("/eligibility/evaluate")
  public EligibilityResponse evaluate(@Valid @RequestBody EligibilityRequest request) {
    requireAdmin();
    return decisionService.evaluate(request);
  }

  @GetMapping(value = "/rules/decision-model", produces = MediaType.APPLICATION_JSON_VALUE)
  public String decisionModel() throws IOException {
    requireAdmin();
    var resource = new ClassPathResource("rules/trade-advance-eligibility.json");
    return resource.getContentAsString(StandardCharsets.UTF_8);
  }
}