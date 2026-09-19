package com.tvscs.rules.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSessionRepository extends JpaRepository<AppSessionEntity, String> {}
