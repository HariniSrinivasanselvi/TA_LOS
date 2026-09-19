package com.tvscs.rules.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {
  Optional<AppUserEntity> findBySubject(String subject);
}
