package com.tvscs.rules.auth;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {
    String traceId = UUID.randomUUID().toString();
    return ResponseEntity.status(e.getStatus())
        .body(
            Map.of(
                "timestamp", Instant.now().toString(),
                "status", e.getStatus().value(),
                "code", e.getCode(),
                "message", e.getMessage(),
                "traceId", traceId));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
    String traceId = UUID.randomUUID().toString();
    log.error("Unhandled exception [traceId={}]", traceId, e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "timestamp", Instant.now().toString(),
                "status", 500,
                "code", "INTERNAL_ERROR",
                "message", "Something went wrong. Please try again.",
                "traceId", traceId));
  }
}
