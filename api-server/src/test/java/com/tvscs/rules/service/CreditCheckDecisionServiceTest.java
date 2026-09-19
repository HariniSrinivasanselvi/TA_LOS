package com.tvscs.rules.service;

import static com.tvscs.rules.model.CreditRuleModels.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CreditCheckDecisionServiceTest {
  private final CreditCheckDecisionService service = new CreditCheckDecisionService();

  private CreditCheckRequest cleanRequest(BorrowerType type, BigDecimal taAmount, int bureauScore) {
    return new CreditCheckRequest(
        "DLR001",
        "Apex Motors",
        type,
        taAmount,
        bureauScore,
        750,
        3,
        false,
        false,
        false,
        true,
        14,
        14,
        true,
        new BigDecimal("700000"),
        true,
        true,
        true,
        false,
        new BigDecimal("15"),
        3,
        new BigDecimal("1"),
        new BigDecimal("2"),
        new BigDecimal("1"),
        new BigDecimal("2"),
        false,
        false,
        true);
  }

  @Test
  void approvesDealerThatPassesEveryCreditCheck() {
    var result = service.evaluate(
        cleanRequest(BorrowerType.INDIVIDUAL_OR_PROPRIETOR, new BigDecimal("500000"), 750));
    assertThat(result.approved()).isTrue();
    assertThat(result.status()).isEqualTo("CREDIT_APPROVED");
    assertThat(result.nextScreen()).isEqualTo("APPLY_FOR_TRADE_ADVANCE");
  }

  @Test
  void declinesOnPolicyBlockButStillRoutesToApplicationScreen() {
    var base = cleanRequest(BorrowerType.DIRECTOR_OR_PARTNER, new BigDecimal("500000"), 720);
    var blocked = new CreditCheckRequest(
        base.dealerCode(), base.dealerName(), base.borrowerType(), base.taAmountRequested(),
        base.bureauScore(), base.commercialCibilScore(), base.cmr(),
        base.delinquency30PlusLast6Months(), base.severeDelinquencyLast3Years(),
        base.onNegativeOrWilfulDefaulterList(), base.cibilConsentObtained(),
        base.businessVintageMonths(), base.tvscsVintageMonths(), base.kycCompleted(),
        base.retailFinanceVolumeLast6Months(), base.requiredFinancialDocumentsSubmitted(),
        base.positivePatLastTwoYears(), base.salesIncreasingTrend(), base.negativeNetWorth(),
        base.penetrationPercentage(), base.positiveTradeReferencesCount(),
        base.thirtyPlusPercentage(), base.nationalAverageThirtyPlusPercentage(),
        base.ninetyPlusPercentage(), base.nationalAverageNinetyPlusPercentage(),
        true, false, base.externalCreditRatingValidated());

    var result = service.evaluate(blocked);
    assertThat(result.approved()).isFalse();
    assertThat(result.status()).isEqualTo("CREDIT_DECLINED");
    assertThat(result.nextScreen()).isEqualTo("APPLY_FOR_TRADE_ADVANCE");
  }

  @Test
  void requiresExternalRatingAboveThreshold() {
    var base = cleanRequest(BorrowerType.DIRECTOR_OR_PARTNER, new BigDecimal("15000000"), 720);
    var unrated = new CreditCheckRequest(
        base.dealerCode(), base.dealerName(), base.borrowerType(), base.taAmountRequested(),
        base.bureauScore(), base.commercialCibilScore(), base.cmr(),
        base.delinquency30PlusLast6Months(), base.severeDelinquencyLast3Years(),
        base.onNegativeOrWilfulDefaulterList(), base.cibilConsentObtained(),
        base.businessVintageMonths(), base.tvscsVintageMonths(), base.kycCompleted(),
        base.retailFinanceVolumeLast6Months(), base.requiredFinancialDocumentsSubmitted(),
        base.positivePatLastTwoYears(), base.salesIncreasingTrend(), base.negativeNetWorth(),
        base.penetrationPercentage(), base.positiveTradeReferencesCount(),
        base.thirtyPlusPercentage(), base.nationalAverageThirtyPlusPercentage(),
        base.ninetyPlusPercentage(), base.nationalAverageNinetyPlusPercentage(),
        base.policyActionedDealer(), base.dealerBlocked(), false);

    var result = service.evaluate(unrated);
    assertThat(result.approved()).isFalse();
  }
}
