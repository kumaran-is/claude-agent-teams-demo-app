package com.fitnessapp.workouts.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String name,
        Instant startedAt,
        Instant completedAt,
        String notes,
        List<SetResponse> sets
) {}
