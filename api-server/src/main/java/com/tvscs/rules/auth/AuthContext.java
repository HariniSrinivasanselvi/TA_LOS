package com.tvscs.rules.auth;

/** Request-scoped holder for the authenticated user, populated by {@link AuthFilter}. */
public final class AuthContext {
  private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

  private AuthContext() {}

  static void set(CurrentUser user) {
    CURRENT.set(user);
  }

  static void clear() {
    CURRENT.remove();
  }

  public static CurrentUser get() {
    return CURRENT.get();
  }

  public static boolean isAuthenticated() {
    return CURRENT.get() != null;
  }
}
