package com.tvscs.rules.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthProperties {
  @Value("${app.auth.issuer-url}")
  private String issuerUrl;

  @Value("${app.auth.client-id}")
  private String clientId;

  @Value("${app.auth.domains}")
  private String domains;

  public String getIssuerUrl() {
    return issuerUrl;
  }

  public String getClientId() {
    return clientId;
  }

  /** First entry of the comma-separated REPLIT_DOMAINS list; used to build the redirect_uri. */
  public String getPrimaryDomain() {
    if (domains == null || domains.isBlank()) return "";
    return domains.split(",")[0].trim();
  }
}
