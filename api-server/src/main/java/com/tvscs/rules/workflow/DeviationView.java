package com.tvscs.rules.workflow;

import java.math.BigDecimal;
import java.time.Instant;

public record DeviationView(
    Long applicationId,
    String dealerName,
    Instant applicationDate,
    BigDecimal requestedAmount,
    String deviationCode,
    String deviationReason,
    String deviationStatus,
    Instant createdDate) {}
