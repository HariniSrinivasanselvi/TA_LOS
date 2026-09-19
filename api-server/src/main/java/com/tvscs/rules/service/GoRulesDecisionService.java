package com.tvscs.rules.service;

import static com.tvscs.rules.model.RuleModels.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class GoRulesDecisionService {
  private final AtomicReference<RuleConfiguration> configuration =
      new AtomicReference<>(new RuleConfiguration(
          6, new BigDecimal("200000"), new BigDecimal("25"), true, 1, Instant.now()));

  public RuleConfiguration getConfiguration() {
    return configuration.get();
  }

  public RuleConfiguration updateConfiguration(RuleConfigurationUpdate update) {
    var current = configuration.get();
    var next = new RuleConfiguration(
        update.minimumVintageMonths(),
        update.minimumThreeMonthAverageDisbursal(),
        update.maximumNetBouncePercentage(),
        update.rejectWhenRcuFraudFlagged(),
        current.version() + 1,
        Instant.now());
    configuration.set(next);
    return next;
  }

  public EligibilityResponse evaluate(EligibilityRequest request) {
    var rules = configuration.get();
    var vintagePassed = request.dealerVintageMonths() >= rules.minimumVintageMonths();
    var salesPassed =
        request.lastThreeMonthAverageDisbursal()
            .compareTo(rules.minimumThreeMonthAverageDisbursal()) > 0;
    var creditPassed =
        request.netBouncePercentage().compareTo(rules.maximumNetBouncePercentage()) < 0;
    var riskPassed = !rules.rejectWhenRcuFraudFlagged() || !request.rcuFraudFlagged();

    var outcomes = List.of(
        new RuleOutcome("VINTAGE", "Dealer vintage", vintagePassed,
            request.dealerVintageMonths() + " months",
            "At least " + rules.minimumVintageMonths() + " months with TVSCS",
            vintagePassed ? "Vintage requirement met" : "Dealer relationship is too new"),
        new RuleOutcome("SALES", "3-month disbursal average", salesPassed,
            formatMoney(request.lastThreeMonthAverageDisbursal()),
            "Greater than " + formatMoney(rules.minimumThreeMonthAverageDisbursal()),
            salesPassed ? "Sales threshold met" : "Average disbursal is below the threshold"),
        new RuleOutcome("CREDIT", "Net bounce percentage", creditPassed,
            request.netBouncePercentage().stripTrailingZeros().toPlainString() + "%",
            "Less than " + rules.maximumNetBouncePercentage().stripTrailingZeros().toPlainString() + "%",
            creditPassed ? "Credit threshold met" : "Net bounce percentage is too high"),
        new RuleOutcome("RISK", "RCU fraud status", riskPassed,
            request.rcuFraudFlagged() ? "Fraud flag raised" : "No fraud flag",
            "No RCU fraud flag",
            riskPassed ? "No blocking RCU fraud flag" : "RCU fraud flag blocks eligibility"));

    var eligible = outcomes.stream().allMatch(RuleOutcome::passed);
    return new EligibilityResponse(
        request.dealerCode(),
        request.dealerName(),
        eligible ? "ELIGIBLE" : "NOT_ELIGIBLE",
        eligible,
        "APPLY_FOR_TRADE_ADVANCE",
        rules.version(),
        Instant.now(),
        outcomes);
  }

  private String formatMoney(BigDecimal value) {
    return "₹" + value.stripTrailingZeros().toPlainString();
  }
}