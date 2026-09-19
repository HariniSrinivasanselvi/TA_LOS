package com.tvscs.rules.api;

import static com.tvscs.rules.model.CreditRuleModels.*;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.AuthContext;
import com.tvscs.rules.service.CreditCheckDecisionService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/** Credit Check / Credit Policy tooling. Admin-only: never exposed on the Dealer journey. */
@RestController
@RequestMapping("/api/credit")
public class CreditCheckController {
  private final CreditCheckDecisionService decisionService;

  public CreditCheckController(CreditCheckDecisionService decisionService) {
    this.decisionService = decisionService;
  }

  private void requireAdmin() {
    var user = AuthContext.get();
    if (user == null) throw ApiException.unauthorized();
    if (!user.isAdmin()) throw ApiException.forbidden("Only Admins can access Credit Check / Credit Policy tools.");
  }

  @GetMapping("/config")
  public CreditCheckConfiguration getConfiguration() {
    requireAdmin();
    return decisionService.getConfiguration();
  }

  @PutMapping("/config")
  public CreditCheckConfiguration updateConfiguration(@Valid @RequestBody CreditCheckConfigurationUpdate update) {
    requireAdmin();
    return decisionService.updateConfiguration(update);
  }

  @PostMapping("/evaluate")
  public CreditCheckResponse evaluate(@Valid @RequestBody CreditCheckRequest request) {
    requireAdmin();
    return decisionService.evaluate(request);
  }

  @GetMapping(value = "/decision-model", produces = MediaType.APPLICATION_JSON_VALUE)
  public String decisionModel() throws IOException {
    requireAdmin();
    var resource = new ClassPathResource("rules/trade-advance-credit-check.json");
    return resource.getContentAsString(StandardCharsets.UTF_8);
  }
}
