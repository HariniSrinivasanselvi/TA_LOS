package com.tvscs.rules.workflow;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "deviation_details")
public class DeviationDetailsEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "deviation_id")
  private Long deviationId;

  @Column(name = "application_id", nullable = false, unique = true)
  private Long applicationId;

  @Column(name = "deviation_code", nullable = false)
  private String deviationCode;

  @Column(name = "deviation_reason")
  private String deviationReason;

  @Enumerated(EnumType.STRING)
  @Column(name = "deviation_status", nullable = false)
  private DeviationStatus deviationStatus;

  @Column(name = "raised_date")
  private Instant raisedDate = Instant.now();

  @Column(name = "reviewed_by")
  private Long reviewedBy;

  @Column(name = "reviewed_date")
  private Instant reviewedDate;

  @Column(name = "admin_remarks")
  private String adminRemarks;

  public Long getDeviationId() {
    return deviationId;
  }

  public Long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Long applicationId) {
    this.applicationId = applicationId;
  }

  public String getDeviationCode() {
    return deviationCode;
  }

  public void setDeviationCode(String deviationCode) {
    this.deviationCode = deviationCode;
  }

  public String getDeviationReason() {
    return deviationReason;
  }

  public void setDeviationReason(String deviationReason) {
    this.deviationReason = deviationReason;
  }

  public DeviationStatus getDeviationStatus() {
    return deviationStatus;
  }

  public void setDeviationStatus(DeviationStatus deviationStatus) {
    this.deviationStatus = deviationStatus;
  }

  public Instant getRaisedDate() {
    return raisedDate;
  }

  public Long getReviewedBy() {
    return reviewedBy;
  }

  public void setReviewedBy(Long reviewedBy) {
    this.reviewedBy = reviewedBy;
  }

  public Instant getReviewedDate() {
    return reviewedDate;
  }

  public void setReviewedDate(Instant reviewedDate) {
    this.reviewedDate = reviewedDate;
  }

  public String getAdminRemarks() {
    return adminRemarks;
  }

  public void setAdminRemarks(String adminRemarks) {
    this.adminRemarks = adminRemarks;
  }
}
