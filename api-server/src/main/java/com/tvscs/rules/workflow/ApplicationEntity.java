package com.tvscs.rules.workflow;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "application_master")
public class ApplicationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "application_id")
  private Long applicationId;

  @Column(name = "dealer_user_id", nullable = false)
  private Long dealerUserId;

  @Column(name = "dealer_code", nullable = false)
  private String dealerCode;

  @Column(name = "dealer_name", nullable = false)
  private String dealerName;

  @Column(name = "dealer_vintage_months", nullable = false)
  private int dealerVintageMonths;

  @Column(name = "last_three_month_average_disbursal", nullable = false)
  private BigDecimal lastThreeMonthAverageDisbursal;

  @Column(name = "net_bounce_percentage")
  private BigDecimal netBouncePercentage;

  @Column(name = "rcu_fraud_flagged", nullable = false)
  private boolean rcuFraudFlagged;

  @Column(name = "requested_amount", nullable = false)
  private BigDecimal requestedAmount;

  @Column(name = "approx_approval_amount")
  private BigDecimal approxApprovalAmount;

  private Boolean eligible;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ApplicationStatus status;

  @Column(name = "approval_tier")
  private String approvalTier;

  @Column(name = "sanctioned_amount")
  private BigDecimal sanctionedAmount;

  @Column(name = "scf_reference")
  private String scfReference;

  @Column(name = "rejection_reason")
  private String rejectionReason;

  @Column(name = "created_date")
  private Instant createdDate = Instant.now();

  @Column(name = "updated_date")
  private Instant updatedDate = Instant.now();

  public Long getApplicationId() {
    return applicationId;
  }

  public Long getDealerUserId() {
    return dealerUserId;
  }

  public void setDealerUserId(Long dealerUserId) {
    this.dealerUserId = dealerUserId;
  }

  public String getDealerCode() {
    return dealerCode;
  }

  public void setDealerCode(String dealerCode) {
    this.dealerCode = dealerCode;
  }

  public String getDealerName() {
    return dealerName;
  }

  public void setDealerName(String dealerName) {
    this.dealerName = dealerName;
  }

  public int getDealerVintageMonths() {
    return dealerVintageMonths;
  }

  public void setDealerVintageMonths(int dealerVintageMonths) {
    this.dealerVintageMonths = dealerVintageMonths;
  }

  public BigDecimal getLastThreeMonthAverageDisbursal() {
    return lastThreeMonthAverageDisbursal;
  }

  public void setLastThreeMonthAverageDisbursal(BigDecimal v) {
    this.lastThreeMonthAverageDisbursal = v;
  }

  public BigDecimal getNetBouncePercentage() {
    return netBouncePercentage;
  }

  public void setNetBouncePercentage(BigDecimal netBouncePercentage) {
    this.netBouncePercentage = netBouncePercentage;
  }

  public boolean isRcuFraudFlagged() {
    return rcuFraudFlagged;
  }

  public void setRcuFraudFlagged(boolean rcuFraudFlagged) {
    this.rcuFraudFlagged = rcuFraudFlagged;
  }

  public BigDecimal getRequestedAmount() {
    return requestedAmount;
  }

  public void setRequestedAmount(BigDecimal requestedAmount) {
    this.requestedAmount = requestedAmount;
  }

  public BigDecimal getApproxApprovalAmount() {
    return approxApprovalAmount;
  }

  public void setApproxApprovalAmount(BigDecimal approxApprovalAmount) {
    this.approxApprovalAmount = approxApprovalAmount;
  }

  public Boolean getEligible() {
    return eligible;
  }

  public void setEligible(Boolean eligible) {
    this.eligible = eligible;
  }

  public ApplicationStatus getStatus() {
    return status;
  }

  public void setStatus(ApplicationStatus status) {
    this.status = status;
    this.updatedDate = Instant.now();
  }

  public String getApprovalTier() {
    return approvalTier;
  }

  public void setApprovalTier(String approvalTier) {
    this.approvalTier = approvalTier;
  }

  public BigDecimal getSanctionedAmount() {
    return sanctionedAmount;
  }

  public void setSanctionedAmount(BigDecimal sanctionedAmount) {
    this.sanctionedAmount = sanctionedAmount;
  }

  public String getScfReference() {
    return scfReference;
  }

  public void setScfReference(String scfReference) {
    this.scfReference = scfReference;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }
}
