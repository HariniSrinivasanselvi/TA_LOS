package com.tvscs.rules.service;

import static com.tvscs.rules.model.CreditRuleModels.*;
import static com.tvscs.rules.model.RuleModels.RuleOutcome;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class CreditCheckDecisionService {
  private final AtomicReference<CreditCheckConfiguration> configuration =
      new AtomicReference<>(new CreditCheckConfiguration(
          730,
          700,
          new BigDecimal("1000000"),
          700,
          6,
          12,
          6,
          new BigDecimal("200000"),
          new BigDecimal("1000000000"),
          new BigDecimal("10000000"),
          new BigDecimal("10"),
          2,
          1,
          Instant.now()));

  public CreditCheckConfiguration getConfiguration() {
    return configuration.get();
  }

  public CreditCheckConfiguration updateConfiguration(CreditCheckConfigurationUpdate update) {
    var current = configuration.get();
    var next = new CreditCheckConfiguration(
        update.minBureauScoreIndividualProprietor(),
        update.minBureauScoreDirectorPartner(),
        update.consumerBureauTaAmountCeiling(),
        update.minCommercialCibilScore(),
        update.maxCmr(),
        update.minBusinessVintageMonths(),
        update.minTvscsVintageMonths(),
        update.minTaLimit(),
        update.maxTaLimit(),
        update.externalRatingRequiredAboveTaAmount(),
        update.minPenetrationPercentage(),
        update.minPositiveTradeReferences(),
        current.version() + 1,
        Instant.now());
    configuration.set(next);
    return next;
  }

  public CreditCheckResponse evaluate(CreditCheckRequest request) {
    var rules = configuration.get();
    List<RuleOutcome> outcomes = new ArrayList<>();

    var usesConsumerBureau =
        request.taAmountRequested().compareTo(rules.consumerBureauTaAmountCeiling()) <= 0;
    boolean bureauScorePassed;
    String bureauActual;
    String bureauExpected;
    if (usesConsumerBureau) {
      var required = request.borrowerType() == BorrowerType.INDIVIDUAL_OR_PROPRIETOR
          ? rules.minBureauScoreIndividualProprietor()
          : rules.minBureauScoreDirectorPartner();
      bureauScorePassed = request.bureauScore() >= required;
      bureauActual = "Consumer bureau score " + request.bureauScore();
      bureauExpected = "At least " + required + " (consumer bureau, TA up to "
          + formatMoney(rules.consumerBureauTaAmountCeiling()) + ")";
    } else {
      var cmrOk = request.cmr() == null || request.cmr() <= rules.maxCmr();
      var scoreOk = request.commercialCibilScore() != null
          && request.commercialCibilScore() > rules.minCommercialCibilScore();
      bureauScorePassed = scoreOk && cmrOk;
      bureauActual = "Commercial CIBIL score "
          + (request.commercialCibilScore() == null ? "not provided" : request.commercialCibilScore())
          + ", CMR " + (request.cmr() == null ? "not provided" : request.cmr());
      bureauExpected = "Commercial score > " + rules.minCommercialCibilScore() + " and CMR <= "
          + rules.maxCmr() + " (TA above " + formatMoney(rules.consumerBureauTaAmountCeiling()) + ")";
    }
    outcomes.add(new RuleOutcome("BUREAU_SCORE", "Bureau score norms", bureauScorePassed,
        bureauActual, bureauExpected,
        bureauScorePassed ? "Bureau score threshold met" : "Bureau score below the required threshold"));

    var conductPassed = !request.delinquency30PlusLast6Months()
        && !request.severeDelinquencyLast3Years()
        && !request.onNegativeOrWilfulDefaulterList();
    outcomes.add(new RuleOutcome("BUREAU_CONDUCT", "CIBIL OD & DPD conduct", conductPassed,
        summarizeConduct(request),
        "No 30+ in last 6 months, no 90+/DBT/SUB/LSS/write-off in last 3 years, not on negative/wilful defaulter list",
        conductPassed ? "Bureau conduct is clean" : "Adverse bureau conduct or negative-list match found"));

    outcomes.add(new RuleOutcome("BUREAU_CONSENT", "CIBIL consent", request.cibilConsentObtained(),
        request.cibilConsentObtained() ? "Consent obtained" : "Consent not obtained",
        "CIBIL consent from individual/directors/partners or sales team confirmation",
        request.cibilConsentObtained() ? "Consent is on file" : "Missing mandatory CIBIL consent"));

    var stabilityPassed = request.businessVintageMonths() >= rules.minBusinessVintageMonths()
        || request.tvscsVintageMonths() >= rules.minTvscsVintageMonths();
    outcomes.add(new RuleOutcome("BUSINESS_STABILITY", "Business continuity & stability", stabilityPassed,
        request.businessVintageMonths() + " months business, " + request.tvscsVintageMonths() + " months with TVSCS",
        "At least " + rules.minBusinessVintageMonths() + " months business stability or "
            + rules.minTvscsVintageMonths() + " months TVSCS vintage",
        stabilityPassed ? "Business continuity requirement met" : "Business is too new on both measures"));

    outcomes.add(new RuleOutcome("KYC", "KYC / application form / photo", request.kycCompleted(),
        request.kycCompleted() ? "KYC complete" : "KYC incomplete",
        "Mandatory KYC, application form and photo",
        request.kycCompleted() ? "KYC is complete" : "KYC documentation is incomplete"));

    var withinAbsoluteRange = request.taAmountRequested().compareTo(rules.minTaLimit()) >= 0
        && request.taAmountRequested().compareTo(rules.maxTaLimit()) <= 0;
    var withinRetailCap = request.retailFinanceVolumeLast6Months() == null
        || request.taAmountRequested().compareTo(
            request.retailFinanceVolumeLast6Months().multiply(new BigDecimal("0.8"))) <= 0;
    var limitRangePassed = withinAbsoluteRange && withinRetailCap;
    outcomes.add(new RuleOutcome("LIMIT_RANGE", "TA limit range", limitRangePassed,
        formatMoney(request.taAmountRequested()) + " requested",
        "Between " + formatMoney(rules.minTaLimit()) + " and " + formatMoney(rules.maxTaLimit())
            + ", within 80% of last 6 months retail finance volume",
        limitRangePassed ? "Requested amount is within policy limits" : "Requested amount is outside policy limits"));

    outcomes.add(new RuleOutcome("FINANCIAL_DOCUMENTS", "Financial documents", request.requiredFinancialDocumentsSubmitted(),
        request.requiredFinancialDocumentsSubmitted() ? "All required documents submitted" : "Documents missing",
        "Financials/ITR, bank statements and GSTR required for the applicable TA tier",
        request.requiredFinancialDocumentsSubmitted() ? "Required financial documents are on file" : "Required financial documents are missing"));

    outcomes.add(new RuleOutcome("FINANCIAL_PAT", "Positive PAT", request.positivePatLastTwoYears(),
        request.positivePatLastTwoYears() ? "Positive PAT" : "Negative or nil PAT",
        "Positive PAT for the last 2 years",
        request.positivePatLastTwoYears() ? "Profitability requirement met" : "PAT was not positive in the last 2 years"));

    outcomes.add(new RuleOutcome("FINANCIAL_SALES_TREND", "Sales trend", request.salesIncreasingTrend(),
        request.salesIncreasingTrend() ? "Increasing trend" : "Flat or declining trend",
        "Sales should be in an increasing trend for the last 2 years",
        request.salesIncreasingTrend() ? "Sales trend requirement met" : "Sales trend is not increasing"));

    var netWorthPassed = !request.negativeNetWorth();
    outcomes.add(new RuleOutcome("FINANCIAL_NET_WORTH", "Net worth", netWorthPassed,
        request.negativeNetWorth() ? "Negative net worth" : "Positive net worth",
        "Negative net worth cases are not allowed as per the last 2 years audited financials",
        netWorthPassed ? "Net worth requirement met" : "Negative net worth disqualifies the dealer"));

    var penetrationPassed = request.penetrationPercentage().compareTo(rules.minPenetrationPercentage()) >= 0;
    outcomes.add(new RuleOutcome("FINANCIAL_PENETRATION", "Penetration", penetrationPassed,
        request.penetrationPercentage().stripTrailingZeros().toPlainString() + "%",
        "At least " + rules.minPenetrationPercentage().stripTrailingZeros().toPlainString()
            + "% over the first 6 months of relationship and thereafter",
        penetrationPassed ? "Penetration requirement met" : "Penetration is below the required minimum"));

    var tradeRefsPassed = request.positiveTradeReferencesCount() >= rules.minPositiveTradeReferences();
    outcomes.add(new RuleOutcome("TRADE_REFERENCES", "Trade references", tradeRefsPassed,
        request.positiveTradeReferencesCount() + " positive references",
        "At least " + rules.minPositiveTradeReferences() + " positive trade references, jointly validated",
        tradeRefsPassed ? "Trade reference requirement met" : "Not enough validated positive trade references"));

    var thirtyPlusPassed = request.thirtyPlusPercentage().compareTo(request.nationalAverageThirtyPlusPercentage()) <= 0;
    var ninetyPlusPassed = request.ninetyPlusPercentage().compareTo(request.nationalAverageNinetyPlusPercentage()) <= 0;
    var portfolioPassed = thirtyPlusPassed && ninetyPlusPassed;
    outcomes.add(new RuleOutcome("PORTFOLIO_TRIGGERS", "Portfolio triggers (30+/90+)", portfolioPassed,
        "30+ " + request.thirtyPlusPercentage().stripTrailingZeros().toPlainString()
            + "%, 90+ " + request.ninetyPlusPercentage().stripTrailingZeros().toPlainString() + "%",
        "30+ and 90+ delinquency at or below the national average for the product",
        portfolioPassed ? "Portfolio delinquency is within the national average" : "Portfolio delinquency exceeds the national average"));

    var riskPassed = !request.policyActionedDealer() && !request.dealerBlocked();
    outcomes.add(new RuleOutcome("POLICY_RISK", "Credit / risk block", riskPassed,
        (request.policyActionedDealer() ? "Policy actioned; " : "") + (request.dealerBlocked() ? "Dealer blocked" : "No block"),
        "Not a restricted/policy actioned dealer and no RCU/collection/policy block",
        riskPassed ? "No blocking credit/risk actions" : "Dealer is blocked or policy actioned"));

    var ratingRequired = request.taAmountRequested().compareTo(rules.externalRatingRequiredAboveTaAmount()) > 0;
    var ratingPassed = !ratingRequired || request.externalCreditRatingValidated();
    outcomes.add(new RuleOutcome("EXTERNAL_RATING", "External credit rating", ratingPassed,
        ratingRequired
            ? (request.externalCreditRatingValidated() ? "Validated" : "Not validated")
            : "Not required",
        "Required and validated for TA above " + formatMoney(rules.externalRatingRequiredAboveTaAmount()),
        ratingPassed ? "External rating requirement satisfied" : "External credit rating is required but not validated"));

    var approved = outcomes.stream().allMatch(RuleOutcome::passed);
    return new CreditCheckResponse(
        request.dealerCode(),
        request.dealerName(),
        approved ? "CREDIT_APPROVED" : "CREDIT_DECLINED",
        approved,
        "APPLY_FOR_TRADE_ADVANCE",
        rules.version(),
        Instant.now(),
        outcomes);
  }

  private String summarizeConduct(CreditCheckRequest request) {
    List<String> flags = new ArrayList<>();
    if (request.delinquency30PlusLast6Months()) flags.add("30+ in last 6 months");
    if (request.severeDelinquencyLast3Years()) flags.add("90+/DBT/SUB/LSS/write-off in last 3 years");
    if (request.onNegativeOrWilfulDefaulterList()) flags.add("negative/wilful defaulter list match");
    return flags.isEmpty() ? "No adverse conduct flags" : String.join(", ", flags);
  }

  private String formatMoney(BigDecimal value) {
    return "₹" + value.stripTrailingZeros().toPlainString();
  }
}
