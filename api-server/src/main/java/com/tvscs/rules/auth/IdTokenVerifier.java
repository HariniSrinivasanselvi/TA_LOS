package com.tvscs.rules.auth;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/** Verifies the RS256-signed ID token returned by the Replit OIDC provider. */
@Service
public class IdTokenVerifier {
  private final OidcDiscoveryService discoveryService;
  private final AuthProperties authProperties;

  public IdTokenVerifier(OidcDiscoveryService discoveryService, AuthProperties authProperties) {
    this.discoveryService = discoveryService;
    this.authProperties = authProperties;
  }

  public record VerifiedClaims(
      String subject, String email, String firstName, String lastName, String profileImageUrl) {}

  public VerifiedClaims verify(String idTokenJwt, String expectedNonce) {
    try {
      SignedJWT jwt = SignedJWT.parse(idTokenJwt);
      String kid = jwt.getHeader().getKeyID();
      JWK jwk = discoveryService.getJwkSet().getKeyByKeyId(kid);
      if (jwk == null) {
        throw new SecurityException("No matching JWK for kid=" + kid);
      }
      JWSVerifier verifier = new RSASSAVerifier((RSAKey) jwk.toPublicJWK());
      if (!jwt.verify(verifier)) {
        throw new SecurityException("ID token signature verification failed");
      }

      var claims = jwt.getJWTClaimsSet();
      String issuer = discoveryService.getIssuer();
      if (claims.getIssuer() != null && !claims.getIssuer().equals(issuer)) {
        throw new SecurityException("Unexpected issuer: " + claims.getIssuer());
      }
      List<String> audience = claims.getAudience();
      if (audience == null || !audience.contains(authProperties.getClientId())) {
        throw new SecurityException("ID token audience does not include this client");
      }
      if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(Instant.now())) {
        throw new SecurityException("ID token has expired");
      }
      Object nonceClaim = claims.getClaim("nonce");
      if (expectedNonce != null && (nonceClaim == null || !expectedNonce.equals(nonceClaim.toString()))) {
        throw new SecurityException("ID token nonce mismatch");
      }

      return new VerifiedClaims(
          claims.getSubject(),
          stringClaim(claims, "email"),
          stringClaim(claims, "first_name"),
          stringClaim(claims, "last_name"),
          stringClaim(claims, "profile_image_url"));
    } catch (SecurityException e) {
      throw e;
    } catch (Exception e) {
      throw new SecurityException("Unable to verify ID token", e);
    }
  }

  private static String stringClaim(com.nimbusds.jwt.JWTClaimsSet claims, String name) {
    Object value = claims.getClaim(name);
    return value != null ? value.toString() : null;
  }
}
