package com.tvscs.rules.workflow;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "audit_id")
  private Long auditId;

  @Column(name = "application_id")
  private Long applicationId;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "action", nullable = false)
  private String action;

  @Column(name = "status")
  private String status;

  @Column(name = "details")
  private String details;

  @Column(name = "created_date")
  private Instant createdDate = Instant.now();

  public AuditLogEntity() {}

  public AuditLogEntity(Long applicationId, Long userId, String action, String status, String details) {
    this.applicationId = applicationId;
    this.userId = userId;
    this.action = action;
    this.status = status;
    this.details = details;
  }
}
