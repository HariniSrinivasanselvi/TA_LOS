package com.tvscs.rules.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final AuthProperties authProperties;
  private final OidcDiscoveryService discoveryService;
  private final LoginStateStore loginStateStore;
  private final IdTokenVerifier idTokenVerifier;
  private final SessionService sessionService;
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AuthController(
      AuthProperties authProperties,
      OidcDiscoveryService discoveryService,
      LoginStateStore loginStateStore,
      IdTokenVerifier idTokenVerifier,
      SessionService sessionService) {
    this.authProperties = authProperties;
    this.discoveryService = discoveryService;
    this.loginStateStore = loginStateStore;
    this.idTokenVerifier = idTokenVerifier;
    this.sessionService = sessionService;
  }

  private String redirectUri() {
    return "https://" + authProperties.getPrimaryDomain() + "/api/callback";
  }

  @GetMapping("/login")
  public void login(@RequestParam(required = false) String returnTo, HttpServletResponse response) throws Exception {
    String codeVerifier = PkceUtil.randomUrlSafeToken(48);
    String codeChallenge = PkceUtil.codeChallengeS256(codeVerifier);
    String state = PkceUtil.randomUrlSafeToken(24);
    String nonce = PkceUtil.randomUrlSafeToken(24);
    loginStateStore.put(state, new LoginStateStore.PendingLogin(codeVerifier, nonce, returnTo != null ? returnTo : "/", Instant.now()));

    String url = discoveryService.getAuthorizationEndpoint()
        + "?client_id=" + enc(authProperties.getClientId())
        + "&response_type=code"
        + "&scope=" + enc("openid profile email")
        + "&redirect_uri=" + enc(redirectUri())
        + "&state=" + enc(state)
        + "&nonce=" + enc(nonce)
        + "&code_challenge=" + enc(codeChallenge)
        + "&code_challenge_method=S256";
    response.sendRedirect(url);
  }

  @GetMapping("/callback")
  public void callback(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String state,
      @RequestParam(required = false, name = "error") String error,
      HttpServletResponse response)
      throws Exception {
    if (error != null || code == null || state == null) {
      response.sendRedirect("/?authError=1");
      return;
    }
    LoginStateStore.PendingLogin pending = loginStateStore.consume(state);
    if (pending == null) {
      response.sendRedirect("/?authError=1");
      return;
    }

    Map<String, String> form =
        Map.of(
            "grant_type", "authorization_code",
            "client_id", authProperties.getClientId(),
            "code", code,
            "redirect_uri", redirectUri(),
            "code_verifier", pending.codeVerifier());
    String body =
        form.entrySet().stream()
            .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
            .reduce((a, b) -> a + "&" + b)
            .orElse("");

    HttpRequest tokenRequest =
        HttpRequest.newBuilder(URI.create(discoveryService.getTokenEndpoint()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
    if (tokenResponse.statusCode() != 200) {
      log.warn("OIDC token exchange failed: {}", tokenResponse.statusCode());
      response.sendRedirect("/?authError=1");
      return;
    }
    JsonNode tokenJson = objectMapper.readTree(tokenResponse.body());
    String idToken = tokenJson.get("id_token").asText();
    String accessToken = tokenJson.has("access_token") ? tokenJson.get("access_token").asText() : null;

    IdTokenVerifier.VerifiedClaims claims = idTokenVerifier.verify(idToken, pending.nonce());
    String sid = sessionService.createSessionForClaims(claims, accessToken);
    sessionService.setSessionCookie(response, sid);
    response.sendRedirect(pending.returnTo());
  }

  @GetMapping("/logout")
  public void logout(@RequestParam(required = false) String returnTo, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    sessionService.deleteSession(request);
    sessionService.clearSessionCookie(response);
    String postLogoutRedirect = "https://" + authProperties.getPrimaryDomain() + (returnTo != null ? returnTo : "/");
    String endSessionEndpoint = discoveryService.getEndSessionEndpoint();
    if (endSessionEndpoint != null) {
      response.sendRedirect(
          endSessionEndpoint + "?client_id=" + enc(authProperties.getClientId()) + "&post_logout_redirect_uri=" + enc(postLogoutRedirect));
    } else {
      response.sendRedirect(postLogoutRedirect);
    }
  }

  @GetMapping("/auth/user")
  public Map<String, Object> currentUser(HttpServletRequest request) {
    return sessionService
        .resolveFromRequest(request)
        .<Map<String, Object>>map(
            u ->
                Map.of(
                    "authenticated", true,
                    "id", u.userId(),
                    "email", u.email() != null ? u.email() : "",
                    "displayName", u.displayName(),
                    "roles", u.roleCodes()))
        .orElse(Map.of("authenticated", false));
  }

  private static String enc(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
