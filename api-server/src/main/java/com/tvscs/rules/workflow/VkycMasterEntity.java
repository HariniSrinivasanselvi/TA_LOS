package com.tvscs.rules.workflow;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vkyc_master")
public class VkycMasterEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "vkyc_id")
  private Long vkycId;

  @Column(name = "application_id", nullable = false, unique = true)
  private Long applicationId;

  @Column(name = "dealer_user_id", nullable = false)
  private Long dealerUserId;

  @Column(name = "dealer_name", nullable = false)
  private String dealerName;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "aadhaar_last4", nullable = false)
  private String aadhaarLast4;

  @Column(name = "aadhaar_hash", nullable = false)
  private String aadhaarHash;

  @Column(name = "pan", nullable = false)
  private String pan;

  @Column(name = "image_data", columnDefinition = "bytea")
  private byte[] imageData;

  @Column(name = "vkyc_status", nullable = false)
  private String vkycStatus;

  @Column(name = "submitted_date")
  private Instant submittedDate = Instant.now();

  @Column(name = "created_by")
  private Long createdBy;

  public Long getVkycId() {
    return vkycId;
  }

  public Long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Long applicationId) {
    this.applicationId = applicationId;
  }

  public Long getDealerUserId() {
    return dealerUserId;
  }

  public void setDealerUserId(Long dealerUserId) {
    this.dealerUserId = dealerUserId;
  }

  public String getDealerName() {
    return dealerName;
  }

  public void setDealerName(String dealerName) {
    this.dealerName = dealerName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAadhaarLast4() {
    return aadhaarLast4;
  }

  public void setAadhaarLast4(String aadhaarLast4) {
    this.aadhaarLast4 = aadhaarLast4;
  }

  public String getAadhaarHash() {
    return aadhaarHash;
  }

  public void setAadhaarHash(String aadhaarHash) {
    this.aadhaarHash = aadhaarHash;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public byte[] getImageData() {
    return imageData;
  }

  public void setImageData(byte[] imageData) {
    this.imageData = imageData;
  }

  public String getVkycStatus() {
    return vkycStatus;
  }

  public void setVkycStatus(String vkycStatus) {
    this.vkycStatus = vkycStatus;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
  }
}
