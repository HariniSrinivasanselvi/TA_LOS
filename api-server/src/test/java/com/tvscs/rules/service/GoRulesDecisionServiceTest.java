package com.tvscs.rules.service;

import static com.tvscs.rules.model.RuleModels.EligibilityRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class GoRulesDecisionServiceTest {
  private final GoRulesDecisionService service = new GoRulesDecisionService();

  @Test
  void approvesDealerThatPassesEveryRule() {
    var result = service.evaluate(new EligibilityRequest(
        "DLR001", "Apex Motors", 12, new BigDecimal("350000"), new BigDecimal("12.5"), false));
    assertThat(result.eligible()).isTrue();
    assertThat(result.nextScreen()).isEqualTo("APPLY_FOR_TRADE_ADVANCE");
  }

  @Test
  void rejectsFraudFlagButStillRoutesToApplicationScreen() {
    var result = service.evaluate(new EligibilityRequest(
        "DLR002", "Metro Motors", 12, new BigDecimal("350000"), new BigDecimal("12.5"), true));
    assertThat(result.eligible()).isFalse();
    assertThat(result.nextScreen()).isEqualTo("APPLY_FOR_TRADE_ADVANCE");
  }
}