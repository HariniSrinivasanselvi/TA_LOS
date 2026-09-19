package com.tvscs.rules.workflow;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
  List<ApplicationEntity> findAllByDealerUserIdOrderByCreatedDateDesc(Long dealerUserId);
}
