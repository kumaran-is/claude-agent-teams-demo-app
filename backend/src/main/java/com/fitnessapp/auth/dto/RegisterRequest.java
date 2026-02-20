package com.fitnessapp.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Optional body for POST /api/v1/auth/register.
 * The Firebase UID and email are taken from the verified JWT claims;
 * only display_name can be optionally provided at registration time.
 */
public record RegisterRequest(
        @Size(max = 255) String displayName
) {}
