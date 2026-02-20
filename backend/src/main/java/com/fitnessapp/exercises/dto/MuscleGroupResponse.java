package com.fitnessapp.exercises.dto;

import java.util.UUID;

public record MuscleGroupResponse(
        UUID id,
        String name,
        String description
) {}
