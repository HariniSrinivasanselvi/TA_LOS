package com.tvscs.rules.auth;

import com.tvscs.rules.rbac.RoleService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {
  public static final String SESSION_COOKIE = "sid";
  private static final long SESSION_TTL_DAYS = 7;

  private final AppUserRepository userRepository;
  private final AppSessionRepository sessionRepository;
  private final RoleService roleService;

  public SessionService(
      AppUserRepository userRepository, AppSessionRepository sessionRepository, RoleService roleService) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
    this.roleService = roleService;
  }

  @Transactional
  public String createSessionForClaims(IdTokenVerifier.VerifiedClaims claims, String accessToken) {
    AppUserEntity user =
        userRepository
            .findBySubject(claims.subject())
            .map(
                existing -> {
                  existing.setEmail(claims.email());
                  existing.setFirstName(claims.firstName());
                  existing.setLastName(claims.lastName());
                  existing.setProfileImageUrl(claims.profileImageUrl());
                  existing.setUpdatedAt(Instant.now());
                  return userRepository.save(existing);
                })
            .orElseGet(
                () -> {
                  AppUserEntity fresh = new AppUserEntity();
                  fresh.setSubject(claims.subject());
                  fresh.setEmail(claims.email());
                  fresh.setFirstName(claims.firstName());
                  fresh.setLastName(claims.lastName());
                  fresh.setProfileImageUrl(claims.profileImageUrl());
                  AppUserEntity saved = userRepository.save(fresh);
                  roleService.assignDefaultRoleForNewUser(saved.getUserId());
                  return saved;
                });

    AppSessionEntity session = new AppSessionEntity();
    session.setSid(PkceUtil.randomUrlSafeToken(32));
    session.setUserId(user.getUserId());
    session.setAccessToken(accessToken);
    session.setExpiresAt(Instant.now().plus(SESSION_TTL_DAYS, ChronoUnit.DAYS));
    sessionRepository.save(session);
    return session.getSid();
  }

  public Optional<CurrentUser> resolveFromRequest(HttpServletRequest request) {
    String sid = readCookie(request, SESSION_COOKIE);
    if (sid == null) return Optional.empty();
    return sessionRepository
        .findById(sid)
        .filter(s -> s.getExpiresAt().isAfter(Instant.now()))
        .flatMap(s -> userRepository.findById(s.getUserId()))
        .map(u -> new CurrentUser(u.getUserId(), u.getSubject(), u.getEmail(), u.displayName(), roleService.getRoleCodesForUser(u.getUserId())));
  }

  @Transactional
  public void deleteSession(HttpServletRequest request) {
    String sid = readCookie(request, SESSION_COOKIE);
    if (sid != null) sessionRepository.deleteById(sid);
  }

  public void setSessionCookie(HttpServletResponse response, String sid) {
    Cookie cookie = new Cookie(SESSION_COOKIE, sid);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setAttribute("SameSite", "Lax");
    cookie.setMaxAge((int) (SESSION_TTL_DAYS * 24 * 60 * 60));
    response.addCookie(cookie);
  }

  public void clearSessionCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(SESSION_COOKIE, "");
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  private static String readCookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null) return null;
    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(name)) return cookie.getValue();
    }
    return null;
  }
}
