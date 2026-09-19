package com.tvscs.rules.workflow;

import java.security.SecureRandom;
import org.springframework.stereotype.Service;

/** Backend-only SCF interface trigger. Simulates the sanctioned-limit hand-off to SCF. */
@Service
public class ScfIntegrationService {
  private final SecureRandom random = new SecureRandom();

  public record ScfResult(boolean success, String reference) {}

  public ScfResult send(ApplicationEntity application) {
    String reference = "SCF-" + (100000 + random.nextInt(900000));
    return new ScfResult(true, reference);
  }
}
