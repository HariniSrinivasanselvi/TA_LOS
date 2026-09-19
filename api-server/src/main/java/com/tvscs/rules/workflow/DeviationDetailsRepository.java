package com.tvscs.rules.workflow;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviationDetailsRepository extends JpaRepository<DeviationDetailsEntity, Long> {
  Optional<DeviationDetailsEntity> findByApplicationId(Long applicationId);

  List<DeviationDetailsEntity> findAllByDeviationStatusOrderByRaisedDateAsc(DeviationStatus status);

  /**
   * Atomic check-and-set: only succeeds if the deviation is still PENDING. Returns the number of
   * rows updated (0 means another admin already decided it) -- this is what makes concurrent
   * approve/reject requests safe without relying on application-level locking.
   */
  @Modifying
  @Query(
      "update DeviationDetailsEntity d set d.deviationStatus = :newStatus, d.reviewedBy = :reviewedBy, "
          + "d.reviewedDate = CURRENT_TIMESTAMP, d.adminRemarks = :remarks "
          + "where d.applicationId = :applicationId and d.deviationStatus = com.tvscs.rules.workflow.DeviationStatus.PENDING")
  int decideIfPending(
      @Param("applicationId") Long applicationId,
      @Param("newStatus") DeviationStatus newStatus,
      @Param("reviewedBy") Long reviewedBy,
      @Param("remarks") String remarks);
}
