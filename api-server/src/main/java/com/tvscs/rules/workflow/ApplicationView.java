package com.tvscs.rules.workflow;

import com.tvscs.rules.model.RuleModels.EligibilityResponse;
import java.math.BigDecimal;
import java.time.Instant;

public record ApplicationView(
    Long applicationId,
    String dealerCode,
    String dealerName,
    BigDecimal requestedAmount,
    BigDecimal approxApprovalAmount,
    Boolean eligible,
    String status,
    String statusLabel,
    boolean deviationPopup,
    String approvalTier,
    BigDecimal sanctionedAmount,
    String scfReference,
    String rejectionReason,
    Instant createdDate,
    EligibilityResponse eligibilityTrace) {}
