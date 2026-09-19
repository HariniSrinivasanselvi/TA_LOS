package com.tvscs.rules.workflow;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Payload for the dealer-facing Apply Now screen. Net Bounce Percentage is intentionally absent:
 * it is no longer collected from the dealer, is not required by validation, and is not part of
 * this payload. Historical values already stored in application_master are left untouched.
 */
public record ApplyRequest(
    @NotBlank String dealerCode,
    @NotBlank String dealerName,
    @Min(0) int dealerVintageMonths,
    @NotNull @DecimalMin("0") BigDecimal lastThreeMonthAverageDisbursal,
    boolean rcuFraudFlagged,
    @NotNull @DecimalMin("0.01") BigDecimal requestedAmount) {}
