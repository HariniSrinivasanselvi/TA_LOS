package com.tvscs.rules.rbac;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
  List<UserRoleEntity> findAllByUserId(Long userId);

  long count();
}
