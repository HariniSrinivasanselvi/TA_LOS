package com.tvscs.rules.workflow;

import com.tvscs.rules.model.CreditRuleModels.BorrowerType;
import com.tvscs.rules.model.CreditRuleModels.CreditCheckRequest;
import com.tvscs.rules.model.CreditRuleModels.CreditCheckResponse;
import com.tvscs.rules.service.CreditCheckDecisionService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/**
 * Backend-only Credit BRE trigger, run automatically after VKYC submission. The dealer-facing
 * workflow no longer exposes a "Run Credit BRE" step; this service derives the credit inputs from
 * data already collected on the application rather than asking the dealer to re-enter them.
 *
 * Fields the streamlined flow does not capture (bureau score, CMR, document/PAT/trend flags,
 * portfolio delinquency ratios, external rating) use conservative "no adverse signal" defaults.
 * In production these would be pulled from a bureau/CRM integration.
 */
@Service
public class CreditBreService {
  private final CreditCheckDecisionService creditCheckDecisionService;

  public CreditBreService(CreditCheckDecisionService creditCheckDecisionService) {
    this.creditCheckDecisionService = creditCheckDecisionService;
  }

  public CreditCheckResponse process(ApplicationEntity application) {
    CreditCheckRequest request =
        new CreditCheckRequest(
            application.getDealerCode(),
            application.getDealerName(),
            BorrowerType.INDIVIDUAL_OR_PROPRIETOR,
            application.getRequestedAmount(),
            750,
            null,
            null,
            false,
            false,
            false,
            true,
            application.getDealerVintageMonths(),
            application.getDealerVintageMonths(),
            true,
            application.getLastThreeMonthAverageDisbursal(),
            true,
            true,
            true,
            false,
            new BigDecimal("15"),
            2,
            new BigDecimal("1"),
            new BigDecimal("2"),
            new BigDecimal("1"),
            new BigDecimal("2"),
            false,
            application.isRcuFraudFlagged(),
            false);
    return creditCheckDecisionService.evaluate(request);
  }
}
