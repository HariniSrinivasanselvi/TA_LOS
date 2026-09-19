package com.tvscs.rules.workflow;

public record VkycView(
    Long applicationId,
    String dealerName,
    String phoneMasked,
    String emailMasked,
    String aadhaarMasked,
    String panMasked,
    String vkycStatus,
    String applicationStatus,
    String applicationStatusLabel,
    String rejectionReason,
    java.math.BigDecimal sanctionedAmount,
    String scfReference) {}
