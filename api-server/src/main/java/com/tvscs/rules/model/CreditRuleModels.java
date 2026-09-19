package com.tvscs.rules.model;

import static com.tvscs.rules.model.RuleModels.RuleOutcome;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class CreditRuleModels {
  private CreditRuleModels() {}

  public enum BorrowerType {
    INDIVIDUAL_OR_PROPRIETOR,
    DIRECTOR_OR_PARTNER
  }

  public record CreditCheckConfiguration(
      @Min(0) int minBureauScoreIndividualProprietor,
      @Min(0) int minBureauScoreDirectorPartner,
      @DecimalMin("0") BigDecimal consumerBureauTaAmountCeiling,
      @Min(0) int minCommercialCibilScore,
      @Min(0) int maxCmr,
      @Min(0) int minBusinessVintageMonths,
      @Min(0) int minTvscsVintageMonths,
      @DecimalMin("0") BigDecimal minTaLimit,
      @DecimalMin("0") BigDecimal maxTaLimit,
      @DecimalMin("0") BigDecimal externalRatingRequiredAboveTaAmount,
      @DecimalMin("0") BigDecimal minPenetrationPercentage,
      @Min(0) int minPositiveTradeReferences,
      long version,
      Instant updatedAt) {}

  public record CreditCheckConfigurationUpdate(
      @Min(0) int minBureauScoreIndividualProprietor,
      @Min(0) int minBureauScoreDirectorPartner,
      @NotNull @DecimalMin("0") BigDecimal consumerBureauTaAmountCeiling,
      @Min(0) int minCommercialCibilScore,
      @Min(0) int maxCmr,
      @Min(0) int minBusinessVintageMonths,
      @Min(0) int minTvscsVintageMonths,
      @NotNull @DecimalMin("0") BigDecimal minTaLimit,
      @NotNull @DecimalMin("0") BigDecimal maxTaLimit,
      @NotNull @DecimalMin("0") BigDecimal externalRatingRequiredAboveTaAmount,
      @NotNull @DecimalMin("0") BigDecimal minPenetrationPercentage,
      @Min(0) int minPositiveTradeReferences) {}

  public record CreditCheckRequest(
      @NotBlank String dealerCode,
      @NotBlank String dealerName,
      @NotNull BorrowerType borrowerType,
      @NotNull @DecimalMin("0") BigDecimal taAmountRequested,

      // Bureau norms
      @Min(0) int bureauScore,
      Integer commercialCibilScore,
      Integer cmr,
      boolean delinquency30PlusLast6Months,
      boolean severeDelinquencyLast3Years,
      boolean onNegativeOrWilfulDefaulterList,
      boolean cibilConsentObtained,

      // Business continuity
      @Min(0) int businessVintageMonths,
      @Min(0) int tvscsVintageMonths,

      // KYC
      boolean kycCompleted,

      // Limit range
      BigDecimal retailFinanceVolumeLast6Months,

      // Financial documents
      boolean requiredFinancialDocumentsSubmitted,

      // Financial assessment
      boolean positivePatLastTwoYears,
      boolean salesIncreasingTrend,
      boolean negativeNetWorth,
      @DecimalMin("0") BigDecimal penetrationPercentage,
      @Min(0) int positiveTradeReferencesCount,

      // Portfolio triggers
      @DecimalMin("0") BigDecimal thirtyPlusPercentage,
      @DecimalMin("0") BigDecimal nationalAverageThirtyPlusPercentage,
      @DecimalMin("0") BigDecimal ninetyPlusPercentage,
      @DecimalMin("0") BigDecimal nationalAverageNinetyPlusPercentage,

      // Credit / risk
      boolean policyActionedDealer,
      boolean dealerBlocked,

      // External credit rating
      boolean externalCreditRatingValidated) {}

  public record CreditCheckResponse(
      String dealerCode,
      String dealerName,
      String status,
      boolean approved,
      String nextScreen,
      long ruleVersion,
      Instant evaluatedAt,
      List<RuleOutcome> outcomes) {}
}
