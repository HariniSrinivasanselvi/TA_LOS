package com.tvscs.rules.workflow;

import com.tvscs.rules.auth.ApiException;
import com.tvscs.rules.auth.CurrentUser;
import com.tvscs.rules.model.RuleModels.EligibilityRequest;
import com.tvscs.rules.model.RuleModels.EligibilityResponse;
import com.tvscs.rules.service.GoRulesDecisionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {
  private static final BigDecimal STP_CAP = new BigDecimal("2500000");
  private static final BigDecimal BUSINESS_HEAD_CAP = new BigDecimal("10000000");
  private static final BigDecimal CEO_CAP = new BigDecimal("200000000");
  private static final BigDecimal STP_DISBURSAL_FACTOR = new BigDecimal("0.85");

  private final ApplicationRepository applicationRepository;
  private final DeviationDetailsRepository deviationRepository;
  private final GoRulesDecisionService eligibilityService;
  private final AuditLogRepository auditLogRepository;

  public ApplicationService(
      ApplicationRepository applicationRepository,
      DeviationDetailsRepository deviationRepository,
      GoRulesDecisionService eligibilityService,
      AuditLogRepository auditLogRepository) {
    this.applicationRepository = applicationRepository;
    this.deviationRepository = deviationRepository;
    this.eligibilityService = eligibilityService;
    this.auditLogRepository = auditLogRepository;
  }

  @Transactional
  public ApplicationView submitApplication(CurrentUser dealer, ApplyRequest request) {
    // Net Bounce Percentage is no longer collected on Apply Now; the shared BRE endpoint still
    // accepts the field, so a neutral default (0) is passed rather than surfacing it to the dealer.
    EligibilityResponse eligibility =
        eligibilityService.evaluate(
            new EligibilityRequest(
                request.dealerCode(),
                request.dealerName(),
                request.dealerVintageMonths(),
                request.lastThreeMonthAverageDisbursal(),
                BigDecimal.ZERO,
                request.rcuFraudFlagged()));

    BigDecimal approxApproval =
        eligibility.eligible()
            ? request.lastThreeMonthAverageDisbursal().multiply(STP_DISBURSAL_FACTOR).setScale(0, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    boolean withinBreAmount = eligibility.eligible() && request.requestedAmount().compareTo(approxApproval) <= 0;
    boolean straightThrough = withinBreAmount && request.requestedAmount().compareTo(STP_CAP) <= 0;

    ApplicationEntity application = new ApplicationEntity();
    application.setDealerUserId(dealer.userId());
    application.setDealerCode(request.dealerCode());
    application.setDealerName(request.dealerName());
    application.setDealerVintageMonths(request.dealerVintageMonths());
    application.setLastThreeMonthAverageDisbursal(request.lastThreeMonthAverageDisbursal());
    application.setRcuFraudFlagged(request.rcuFraudFlagged());
    application.setRequestedAmount(request.requestedAmount());
    application.setApproxApprovalAmount(approxApproval);
    application.setEligible(eligibility.eligible());
    application.setApprovalTier(approvalTierFor(request.requestedAmount()));

    if (straightThrough) {
      application.setStatus(ApplicationStatus.VKYC_PENDING);
    } else {
      application.setStatus(ApplicationStatus.DEVIATION_PENDING);
    }
    applicationRepository.save(application);

    if (!straightThrough) {
      DeviationDetailsEntity deviation = new DeviationDetailsEntity();
      deviation.setApplicationId(application.getApplicationId());
      deviation.setDeviationCode(eligibility.eligible() ? "AMOUNT_EXCEEDS_STP" : "BRE_INELIGIBLE");
      deviation.setDeviationReason(
          eligibility.eligible()
              ? "Requested amount exceeds the BRE-approved amount or the straight-through cap; requires "
                  + application.getApprovalTier() + "."
              : "Eligibility (BRE) check failed.");
      deviation.setDeviationStatus(DeviationStatus.PENDING);
      deviationRepository.save(deviation);
    }

    auditLogRepository.save(
        new AuditLogEntity(
            application.getApplicationId(), dealer.userId(), "APPLICATION_SUBMITTED", application.getStatus().name(), null));

    return toView(application, eligibility);
  }

  public List<ApplicationView> listMine(CurrentUser dealer) {
    return applicationRepository.findAllByDealerUserIdOrderByCreatedDateDesc(dealer.userId()).stream()
        .map(a -> toView(a, null))
        .toList();
  }

  public ApplicationView getForCurrentUser(Long applicationId, CurrentUser user) {
    ApplicationEntity application = requireOwnedOrAdmin(applicationId, user);
    return toView(application, null);
  }

  ApplicationEntity requireOwnedOrAdmin(Long applicationId, CurrentUser user) {
    ApplicationEntity application =
        applicationRepository.findById(applicationId).orElseThrow(() -> ApiException.notFound("Application not found."));
    if (!user.isAdmin() && !application.getDealerUserId().equals(user.userId())) {
      throw ApiException.forbidden("You can only access your own applications.");
    }
    return application;
  }

  ApplicationRepository repository() {
    return applicationRepository;
  }

  private String approvalTierFor(BigDecimal requested) {
    if (requested.compareTo(CEO_CAP) > 0) return "Credit Sanction Committee (CSC) of the Board";
    if (requested.compareTo(BUSINESS_HEAD_CAP) > 0) return "CEO approval";
    if (requested.compareTo(STP_CAP) > 0) return "Business Head approval";
    return "Business SPOC (Product Team) verification";
  }

  ApplicationView toView(ApplicationEntity a, EligibilityResponse trace) {
    boolean deviationPopup = a.getStatus() == ApplicationStatus.DEVIATION_PENDING;
    return new ApplicationView(
        a.getApplicationId(),
        a.getDealerCode(),
        a.getDealerName(),
        a.getRequestedAmount(),
        a.getApproxApprovalAmount(),
        a.getEligible(),
        a.getStatus().name(),
        a.getStatus().dealerFacingLabel(),
        deviationPopup,
        a.getApprovalTier(),
        a.getSanctionedAmount(),
        a.getScfReference(),
        a.getRejectionReason(),
        a.getCreatedDate(),
        trace);
  }
}
