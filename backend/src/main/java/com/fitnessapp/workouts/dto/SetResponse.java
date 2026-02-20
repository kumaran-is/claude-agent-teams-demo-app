package com.fitnessapp.workouts.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SetResponse(
        UUID id,
        UUID exerciseId,
        int setNumber,
        int reps,
        BigDecimal weightKg,
        Instant completedAt
) {}
