package com.tvscs.rules.workflow;

import jakarta.validation.constraints.NotBlank;

public record VkycRequest(
    @NotBlank String dealerName,
    @NotBlank String phoneNumber,
    @NotBlank String email,
    @NotBlank String aadhaar,
    @NotBlank String pan,
    /** Data URI (data:image/jpeg;base64,...) captured from the browser camera. */
    @NotBlank String imageDataUrl) {}
