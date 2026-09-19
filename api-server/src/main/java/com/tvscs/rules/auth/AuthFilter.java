package com.tvscs.rules.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Loads the authenticated user (if any) from the session cookie on every request. */
@Component
public class AuthFilter extends OncePerRequestFilter {
  private final SessionService sessionService;

  public AuthFilter(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      sessionService.resolveFromRequest(request).ifPresent(AuthContext::set);
      filterChain.doFilter(request, response);
    } finally {
      AuthContext.clear();
    }
  }
}
