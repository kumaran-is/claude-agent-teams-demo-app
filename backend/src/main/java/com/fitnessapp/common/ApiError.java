package com.fitnessapp.common;

import java.time.Instant;

/**
 * Standard error response body.
 * Controllers do not use this directly — GlobalExceptionHandler returns ProblemDetail (RFC 9457).
 * Retained for legacy clients that may expect this shape.
 */
public record ApiError(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {}
