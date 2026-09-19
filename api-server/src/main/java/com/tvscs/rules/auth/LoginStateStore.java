package com.tvscs.rules.auth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Short-lived, in-memory store correlating the OIDC `state` param with the PKCE verifier and nonce for one login attempt. */
@Component
public class LoginStateStore {
  public record PendingLogin(String codeVerifier, String nonce, String returnTo, Instant createdAt) {}

  private final Map<String, PendingLogin> pending = new ConcurrentHashMap<>();

  public void put(String state, PendingLogin login) {
    cleanupExpired();
    pending.put(state, login);
  }

  public PendingLogin consume(String state) {
    return pending.remove(state);
  }

  private void cleanupExpired() {
    Instant cutoff = Instant.now().minusSeconds(600);
    pending.entrySet().removeIf(e -> e.getValue().createdAt().isBefore(cutoff));
  }
}
