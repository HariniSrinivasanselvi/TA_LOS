package com.tvscs.rules.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Service;

/** Fetches and caches the Replit OIDC discovery document and its JWK set. */
@Service
public class OidcDiscoveryService {
  private final AuthProperties authProperties;
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  private volatile JsonNode discoveryDocument;
  private volatile JWKSet jwkSet;
  private volatile long fetchedAtEpochMs;
  private static final long CACHE_TTL_MS = Duration.ofHours(1).toMillis();

  public OidcDiscoveryService(AuthProperties authProperties) {
    this.authProperties = authProperties;
  }

  public synchronized JsonNode getDiscoveryDocument() {
    ensureFresh();
    return discoveryDocument;
  }

  public synchronized JWKSet getJwkSet() {
    ensureFresh();
    return jwkSet;
  }

  private void ensureFresh() {
    if (discoveryDocument != null && System.currentTimeMillis() - fetchedAtEpochMs < CACHE_TTL_MS) return;
    try {
      String wellKnown = authProperties.getIssuerUrl() + "/.well-known/openid-configuration";
      HttpRequest request = HttpRequest.newBuilder(URI.create(wellKnown)).GET().build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new IllegalStateException("OIDC discovery failed with status " + response.statusCode());
      }
      discoveryDocument = objectMapper.readTree(response.body());

      String jwksUri = discoveryDocument.get("jwks_uri").asText();
      HttpRequest jwksRequest = HttpRequest.newBuilder(URI.create(jwksUri)).GET().build();
      HttpResponse<String> jwksResponse = httpClient.send(jwksRequest, HttpResponse.BodyHandlers.ofString());
      jwkSet = JWKSet.parse(jwksResponse.body());
      fetchedAtEpochMs = System.currentTimeMillis();
    } catch (Exception e) {
      if (discoveryDocument == null) {
        throw new IllegalStateException("Unable to load OIDC discovery document", e);
      }
      // keep serving the stale cache if a refresh attempt fails
    }
  }

  public String getAuthorizationEndpoint() {
    return getDiscoveryDocument().get("authorization_endpoint").asText();
  }

  public String getTokenEndpoint() {
    return getDiscoveryDocument().get("token_endpoint").asText();
  }

  public String getEndSessionEndpoint() {
    JsonNode node = getDiscoveryDocument().get("end_session_endpoint");
    return node != null ? node.asText() : null;
  }

  public String getIssuer() {
    return getDiscoveryDocument().get("issuer").asText();
  }
}
