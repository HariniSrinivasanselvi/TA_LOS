package com.tvscs.rules.workflow;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.CurrentUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin-only deviation review. Never exposed to Dealers as a workflow screen. */
@Service
public class DeviationService {
  private final DeviationDetailsRepository deviationRepository;
  private final ApplicationRepository applicationRepository;
  private final AuditLogRepository auditLogRepository;

  public DeviationService(
      DeviationDetailsRepository deviationRepository,
      ApplicationRepository applicationRepository,
      AuditLogRepository auditLogRepository) {
    this.deviationRepository = deviationRepository;
    this.applicationRepository = applicationRepository;
    this.auditLogRepository = auditLogRepository;
  }

  public List<DeviationView> listPending() {
    return deviationRepository.findAllByDeviationStatusOrderByRaisedDateAsc(DeviationStatus.PENDING).stream()
        .map(this::toView)
        .toList();
  }

  @Transactional
  public DeviationView approve(Long applicationId, CurrentUser admin, String remarks) {
    ApplicationEntity application =
        applicationRepository.findById(applicationId).orElseThrow(() -> ApiException.notFound("Application not found."));

    // A hard RCU fraud flag can never be overridden by a deviation approval, regardless of the
    // reviewing admin's decision -- this mirrors the non-negotiable credit-policy rule.
    if (application.isRcuFraudFlagged()) {
      int rows = deviationRepository.decideIfPending(applicationId, DeviationStatus.REJECTED, admin.userId(),
          "Auto-rejected: RCU fraud flag cannot be overridden by deviation approval.");
      if (rows == 0) throw ApiException.conflict("ALREADY_PROCESSED", "This application has already been processed.");
      application.setStatus(ApplicationStatus.DEVIATION_REJECTED);
      application.setRejectionReason("Your request for trade advance is rejected from company side due to credit policy.");
      applicationRepository.save(application);
      auditLogRepository.save(new AuditLogEntity(applicationId, admin.userId(), "DEVIATION_AUTO_REJECTED_RCU", "REJECTED", null));
      throw ApiException.badRequest(
          "RCU_FRAUD_BLOCK", "This application has an active RCU fraud flag and cannot be approved. It has been rejected automatically.");
    }

    int rows = deviationRepository.decideIfPending(applicationId, DeviationStatus.APPROVED, admin.userId(), remarks);
    if (rows == 0) throw ApiException.conflict("ALREADY_PROCESSED", "This application has already been processed.");

    application.setStatus(ApplicationStatus.VKYC_PENDING);
    applicationRepository.save(application);
    auditLogRepository.save(new AuditLogEntity(applicationId, admin.userId(), "DEVIATION_APPROVED", "APPROVED", remarks));
    return toView(deviationRepository.findByApplicationId(applicationId).orElseThrow());
  }

  @Transactional
  public DeviationView reject(Long applicationId, CurrentUser admin, String remarks) {
    ApplicationEntity application =
        applicationRepository.findById(applicationId).orElseThrow(() -> ApiException.notFound("Application not found."));

    int rows = deviationRepository.decideIfPending(applicationId, DeviationStatus.REJECTED, admin.userId(), remarks);
    if (rows == 0) throw ApiException.conflict("ALREADY_PROCESSED", "This application has already been processed.");

    application.setStatus(ApplicationStatus.DEVIATION_REJECTED);
    application.setRejectionReason("Your request for trade advance is rejected from company side due to credit policy.");
    applicationRepository.save(application);
    auditLogRepository.save(new AuditLogEntity(applicationId, admin.userId(), "DEVIATION_REJECTED", "REJECTED", remarks));
    return toView(deviationRepository.findByApplicationId(applicationId).orElseThrow());
  }

  private DeviationView toView(DeviationDetailsEntity deviation) {
    ApplicationEntity application = applicationRepository.findById(deviation.getApplicationId()).orElseThrow();
    return new DeviationView(
        application.getApplicationId(),
        application.getDealerName(),
        application.getCreatedDate(),
        application.getRequestedAmount(),
        deviation.getDeviationCode(),
        deviation.getDeviationReason(),
        deviation.getDeviationStatus().name(),
        deviation.getRaisedDate());
  }
}
