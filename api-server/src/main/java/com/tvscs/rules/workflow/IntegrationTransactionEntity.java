package com.tvscs.rules.workflow;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "integration_transaction")
public class IntegrationTransactionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "integration_id")
  private Long integrationId;

  @Column(name = "application_id", nullable = false)
  private Long applicationId;

  @Column(name = "vkyc_id")
  private Long vkycId;

  @Column(name = "integration_type", nullable = false)
  private String integrationType;

  @Column(name = "request_reference")
  private String requestReference;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "retry_count")
  private int retryCount;

  @Column(name = "last_attempt_date")
  private Instant lastAttemptDate = Instant.now();

  @Column(name = "response_code")
  private String responseCode;

  @Column(name = "error_message")
  private String errorMessage;

  public Long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Long applicationId) {
    this.applicationId = applicationId;
  }

  public Long getVkycId() {
    return vkycId;
  }

  public void setVkycId(Long vkycId) {
    this.vkycId = vkycId;
  }

  public String getIntegrationType() {
    return integrationType;
  }

  public void setIntegrationType(String integrationType) {
    this.integrationType = integrationType;
  }

  public String getRequestReference() {
    return requestReference;
  }

  public void setRequestReference(String requestReference) {
    this.requestReference = requestReference;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(String responseCode) {
    this.responseCode = responseCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
