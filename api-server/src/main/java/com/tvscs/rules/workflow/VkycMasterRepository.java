package com.tvscs.rules.workflow;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VkycMasterRepository extends JpaRepository<VkycMasterEntity, Long> {
  Optional<VkycMasterEntity> findByApplicationId(Long applicationId);
}
