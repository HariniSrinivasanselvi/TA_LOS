package com.tvscs.rules.workflow;

public enum ApplicationStatus {
  SUBMITTED,
  ELIGIBILITY_CHECK,
  DEVIATION_PENDING,
  DEVIATION_APPROVED,
  DEVIATION_REJECTED,
  VKYC_PENDING,
  VKYC_IN_PROGRESS,
  VKYC_COMPLETED,
  CREDIT_BRE_PROCESSING,
  SCF_PROCESSING,
  COMPLETED,
  FAILED;

  /** Short, non-technical copy shown to the Dealer. Internal integration states are never exposed. */
  public String dealerFacingLabel() {
    return switch (this) {
      case SUBMITTED, ELIGIBILITY_CHECK -> "Application submitted";
      case DEVIATION_PENDING -> "Deviation review pending";
      case DEVIATION_APPROVED -> "Deviation approved";
      case DEVIATION_REJECTED -> "Application rejected";
      case VKYC_PENDING -> "VKYC pending";
      case VKYC_IN_PROGRESS -> "VKYC in progress";
      case VKYC_COMPLETED, CREDIT_BRE_PROCESSING, SCF_PROCESSING -> "Processing your application";
      case COMPLETED -> "Trade advance approved";
      case FAILED -> "Application could not be completed";
    };
  }
}
