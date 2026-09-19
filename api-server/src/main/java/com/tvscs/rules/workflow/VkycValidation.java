package com.tvscs.rules.workflow;

import com.tvscs.rules.auth.ApiException;
import java.util.regex.Pattern;

/** Server-side VKYC field validation. Frontend validation is never trusted on its own. */
final class VkycValidation {
  private VkycValidation() {}

  private static final Pattern INDIAN_MOBILE = Pattern.compile("^[6-9]\\d{9}$");
  private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final Pattern AADHAAR = Pattern.compile("^\\d{12}$");
  private static final Pattern PAN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
  private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

  static String requireName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw ApiException.badRequest("VKYC_INVALID_NAME", "Dealer name is required.");
    }
    return name.trim();
  }

  static String requirePhone(String phone) {
    String normalized = phone == null ? "" : phone.replaceAll("[^0-9]", "");
    if (normalized.length() == 12 && normalized.startsWith("91")) normalized = normalized.substring(2);
    if (!INDIAN_MOBILE.matcher(normalized).matches()) {
      throw ApiException.badRequest("VKYC_INVALID_PHONE", "Enter a valid 10-digit Indian mobile number.");
    }
    return normalized;
  }

  static String requireEmail(String email) {
    if (email == null || !EMAIL.matcher(email.trim()).matches()) {
      throw ApiException.badRequest("VKYC_INVALID_EMAIL", "Enter a valid email address.");
    }
    return email.trim();
  }

  static String requireAadhaar(String aadhaar) {
    String normalized = aadhaar == null ? "" : aadhaar.replaceAll("[^0-9]", "");
    if (!AADHAAR.matcher(normalized).matches()) {
      throw ApiException.badRequest("VKYC_INVALID_AADHAAR", "Enter a valid 12-digit Aadhaar number.");
    }
    return normalized;
  }

  static String requirePan(String pan) {
    String normalized = pan == null ? "" : pan.trim().toUpperCase();
    if (!PAN.matcher(normalized).matches()) {
      throw ApiException.badRequest("VKYC_INVALID_PAN", "Enter a valid PAN (e.g. ABCDE1234F).");
    }
    return normalized;
  }

  static byte[] decodeImage(String dataUrl) {
    if (dataUrl == null || !dataUrl.startsWith("data:image/")) {
      throw ApiException.badRequest("VKYC_INVALID_IMAGE", "A live captured image is required.");
    }
    int commaIndex = dataUrl.indexOf(',');
    if (commaIndex < 0) {
      throw ApiException.badRequest("VKYC_INVALID_IMAGE", "The captured image is not valid.");
    }
    byte[] bytes;
    try {
      bytes = java.util.Base64.getDecoder().decode(dataUrl.substring(commaIndex + 1));
    } catch (IllegalArgumentException e) {
      throw ApiException.badRequest("VKYC_INVALID_IMAGE", "The captured image is not valid.");
    }
    if (bytes.length == 0) {
      throw ApiException.badRequest("VKYC_INVALID_IMAGE", "A live captured image is required.");
    }
    if (bytes.length > MAX_IMAGE_BYTES) {
      throw ApiException.badRequest("VKYC_IMAGE_TOO_LARGE", "The captured image is too large. Please retake it.");
    }
    return bytes;
  }
}
