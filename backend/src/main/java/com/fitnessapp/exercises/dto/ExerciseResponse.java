package com.fitnessapp.exercises.dto;

import java.time.Instant;
import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        UUID muscleGroupId,
        String muscleGroupName,
        String instructions,
        UUID createdByUserId,
        Instant createdAt
) {}
