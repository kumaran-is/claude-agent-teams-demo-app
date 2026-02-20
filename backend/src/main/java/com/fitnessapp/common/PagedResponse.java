package com.fitnessapp.common;

import java.util.List;

/**
 * Generic wrapper for paginated list responses.
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {}
