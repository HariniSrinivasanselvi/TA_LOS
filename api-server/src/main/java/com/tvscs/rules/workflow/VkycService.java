package com.tvscs.rules.workflow;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.CurrentUser;
import com.tvscs.rules.model.CreditRuleModels.CreditCheckResponse;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates VKYC submission and the backend-only Credit BRE + SCF processing that follows it.
 * Neither Credit BRE nor SCF are exposed as frontend steps -- this service owns that sequencing.
 */
@Service
public class VkycService {
  // Deliberately does not log Aadhaar/PAN or the raw request body -- only status transitions.
  private static final Logger log = LoggerFactory.getLogger(VkycService.class);

  private final ApplicationRepository applicationRepository;
  private final VkycMasterRepository vkycRepository;
  private final IntegrationTransactionRepository integrationRepository;
  private final AuditLogRepository auditLogRepository;
  private final CreditBreService creditBreService;
  private final ScfIntegrationService scfIntegrationService;

  public VkycService(
      ApplicationRepository applicationRepository,
      VkycMasterRepository vkycRepository,
      IntegrationTransactionRepository integrationRepository,
      AuditLogRepository auditLogRepository,
      CreditBreService creditBreService,
      ScfIntegrationService scfIntegrationService) {
    this.applicationRepository = applicationRepository;
    this.vkycRepository = vkycRepository;
    this.integrationRepository = integrationRepository;
    this.auditLogRepository = auditLogRepository;
    this.creditBreService = creditBreService;
    this.scfIntegrationService = scfIntegrationService;
  }

  @Transactional
  public VkycView submitVkyc(Long applicationId, CurrentUser dealer, VkycRequest request) {
    ApplicationEntity application =
        applicationRepository.findById(applicationId).orElseThrow(() -> ApiException.notFound("Application not found."));
    if (!application.getDealerUserId().equals(dealer.userId())) {
      throw ApiException.forbidden("You can only submit VKYC for your own application.");
    }
    if (application.getStatus() != ApplicationStatus.VKYC_PENDING) {
      if (vkycRepository.findByApplicationId(applicationId).isPresent()) {
        throw ApiException.conflict("VKYC_ALREADY_PROCESSED", "This application has already been processed.");
      }
      throw ApiException.badRequest("VKYC_NOT_ELIGIBLE", "This application is not eligible for VKYC yet.");
    }

    String dealerName = VkycValidation.requireName(request.dealerName());
    String phone = VkycValidation.requirePhone(request.phoneNumber());
    String email = VkycValidation.requireEmail(request.email());
    String aadhaar = VkycValidation.requireAadhaar(request.aadhaar());
    String pan = VkycValidation.requirePan(request.pan());
    byte[] imageBytes = VkycValidation.decodeImage(request.imageDataUrl());

    // Idempotency: flip VKYC_PENDING -> VKYC_IN_PROGRESS only if it is still pending, so a
    // double-click cannot race two submissions for the same application.
    application.setStatus(ApplicationStatus.VKYC_IN_PROGRESS);
    applicationRepository.save(application);

    VkycMasterEntity vkyc = new VkycMasterEntity();
    vkyc.setApplicationId(applicationId);
    vkyc.setDealerUserId(dealer.userId());
    vkyc.setDealerName(dealerName);
    vkyc.setPhoneNumber(phone);
    vkyc.setEmail(email);
    vkyc.setAadhaarLast4(aadhaar.substring(8));
    vkyc.setAadhaarHash(sha256(aadhaar));
    vkyc.setPan(pan);
    vkyc.setImageData(imageBytes);
    vkyc.setVkycStatus("COMPLETED");
    vkyc.setCreatedBy(dealer.userId());
    try {
      vkycRepository.save(vkyc);
    } catch (DataIntegrityViolationException e) {
      throw ApiException.conflict("VKYC_ALREADY_PROCESSED", "This application has already been processed.");
    }

    application.setStatus(ApplicationStatus.VKYC_COMPLETED);
    applicationRepository.save(application);
    auditLogRepository.save(new AuditLogEntity(applicationId, dealer.userId(), "VKYC_SUBMITTED", "COMPLETED", null));

    runBackendOrchestration(application, vkyc);
    return toView(application, vkyc);
  }

  /**
   * Credit BRE and SCF run here, backend-only, immediately after VKYC persists. Never expose these
   * as separate dealer-facing steps or buttons.
   */
  private void runBackendOrchestration(ApplicationEntity application, VkycMasterEntity vkyc) {
    application.setStatus(ApplicationStatus.CREDIT_BRE_PROCESSING);
    applicationRepository.save(application);

    CreditCheckResponse creditResult;
    try {
      creditResult = creditBreService.process(application);
    } catch (Exception e) {
      log.warn("Credit BRE processing failed for application {}", application.getApplicationId(), e);
      recordIntegration(application, vkyc, "CREDIT_BRE", "FAILED", null, "ERROR", "Credit BRE processing error");
      failApplication(application, "Unable to complete credit processing. Please try again.");
      return;
    }

    recordIntegration(
        application, vkyc, "CREDIT_BRE", creditResult.approved() ? "SUCCESS" : "FAILED", null,
        creditResult.approved() ? "200" : "400", creditResult.approved() ? null : "Credit policy checks failed");

    if (!creditResult.approved()) {
      failApplication(application, "Your request for trade advance is rejected from company side due to credit policy.");
      return;
    }

    application.setStatus(ApplicationStatus.SCF_PROCESSING);
    applicationRepository.save(application);

    ScfIntegrationService.ScfResult scfResult;
    try {
      scfResult = scfIntegrationService.send(application);
    } catch (Exception e) {
      log.warn("SCF interface failed for application {}", application.getApplicationId(), e);
      recordIntegration(application, vkyc, "SCF", "FAILED", null, "ERROR", "SCF interface error");
      failApplication(application, "Credit approved, but the SCF interface failed. Please contact support to retry.");
      return;
    }

    recordIntegration(application, vkyc, "SCF", "SUCCESS", scfResult.reference(), "200", null);

    application.setSanctionedAmount(application.getRequestedAmount());
    application.setScfReference(scfResult.reference());
    application.setStatus(ApplicationStatus.COMPLETED);
    applicationRepository.save(application);
    auditLogRepository.save(
        new AuditLogEntity(application.getApplicationId(), null, "APPLICATION_COMPLETED", "COMPLETED", scfResult.reference()));
  }

  private void failApplication(ApplicationEntity application, String reason) {
    application.setStatus(ApplicationStatus.FAILED);
    application.setRejectionReason(reason);
    applicationRepository.save(application);
    auditLogRepository.save(new AuditLogEntity(application.getApplicationId(), null, "APPLICATION_FAILED", "FAILED", reason));
  }

  private void recordIntegration(
      ApplicationEntity application,
      VkycMasterEntity vkyc,
      String type,
      String status,
      String reference,
      String responseCode,
      String errorMessage) {
    IntegrationTransactionEntity tx = new IntegrationTransactionEntity();
    tx.setApplicationId(application.getApplicationId());
    tx.setVkycId(vkyc.getVkycId());
    tx.setIntegrationType(type);
    tx.setRequestReference(reference);
    tx.setStatus(status);
    tx.setResponseCode(responseCode);
    tx.setErrorMessage(errorMessage);
    integrationRepository.save(tx);
  }

  public VkycView getForCurrentUser(Long applicationId, CurrentUser user) {
    ApplicationEntity application =
        applicationRepository.findById(applicationId).orElseThrow(() -> ApiException.notFound("Application not found."));
    if (!user.isAdmin() && !application.getDealerUserId().equals(user.userId())) {
      throw ApiException.forbidden("You can only access your own applications.");
    }
    VkycMasterEntity vkyc =
        vkycRepository.findByApplicationId(applicationId).orElseThrow(() -> ApiException.notFound("VKYC record not found."));
    return toView(application, vkyc);
  }

  private VkycView toView(ApplicationEntity application, VkycMasterEntity vkyc) {
    return new VkycView(
        application.getApplicationId(),
        vkyc.getDealerName(),
        maskPhone(vkyc.getPhoneNumber()),
        maskEmail(vkyc.getEmail()),
        "XXXX XXXX " + vkyc.getAadhaarLast4(),
        maskPan(vkyc.getPan()),
        vkyc.getVkycStatus(),
        application.getStatus().name(),
        application.getStatus().dealerFacingLabel(),
        application.getRejectionReason(),
        application.getSanctionedAmount(),
        application.getScfReference());
  }

  private static String maskPhone(String phone) {
    return "XXXXXX" + phone.substring(Math.max(0, phone.length() - 4));
  }

  private static String maskEmail(String email) {
    int at = email.indexOf('@');
    if (at <= 1) return email;
    return email.charAt(0) + "***" + email.substring(at);
  }

  private static String maskPan(String pan) {
    return pan.substring(0, 2) + "XXXXX" + pan.substring(pan.length() - 2);
  }

  private static String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
