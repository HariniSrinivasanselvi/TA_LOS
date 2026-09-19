package com.tvscs.rules.rbac;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
  List<MenuEntity> findAllByMenuIdIn(List<Long> menuIds);

  Optional<MenuEntity> findByMenuCode(String menuCode);
}
