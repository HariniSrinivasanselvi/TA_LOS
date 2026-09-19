package com.tvscs.rules.rbac;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleMenuMappingRepository extends JpaRepository<RoleMenuMappingEntity, Long> {
  List<RoleMenuMappingEntity> findAllByRoleIdInAndActiveTrue(List<Long> roleIds);
}
