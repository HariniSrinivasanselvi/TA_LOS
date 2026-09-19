package com.tvscs.rules.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class RuleModels {
  private RuleModels() {}

  public record RuleConfiguration(
      @Min(0) int minimumVintageMonths,
      @DecimalMin("0") BigDecimal minimumThreeMonthAverageDisbursal,
      @DecimalMin("0") BigDecimal maximumNetBouncePercentage,
      boolean rejectWhenRcuFraudFlagged,
      long version,
      Instant updatedAt) {}

  public record RuleConfigurationUpdate(
      @Min(0) int minimumVintageMonths,
      @NotNull @DecimalMin("0") BigDecimal minimumThreeMonthAverageDisbursal,
      @NotNull @DecimalMin("0") BigDecimal maximumNetBouncePercentage,
      boolean rejectWhenRcuFraudFlagged) {}

  public record EligibilityRequest(
      @NotBlank String dealerCode,
      @NotBlank String dealerName,
      @Min(0) int dealerVintageMonths,
      @NotNull @DecimalMin("0") BigDecimal lastThreeMonthAverageDisbursal,
      @NotNull @DecimalMin("0") BigDecimal netBouncePercentage,
      boolean rcuFraudFlagged) {}

  public record RuleOutcome(
      String ruleId,
      String label,
      boolean passed,
      String actual,
      String expected,
      String reason) {}

  public record EligibilityResponse(
      String dealerCode,
      String dealerName,
      String status,
      boolean eligible,
      String nextScreen,
      long ruleVersion,
      Instant evaluatedAt,
      List<RuleOutcome> outcomes) {}
}