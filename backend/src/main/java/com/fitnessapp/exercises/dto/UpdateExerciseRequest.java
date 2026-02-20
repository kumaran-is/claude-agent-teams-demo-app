package com.fitnessapp.exercises.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateExerciseRequest(
        @Size(max = 255) String name,
        @Size(max = 2000) String description,
        UUID muscleGroupId,
        @Size(max = 5000) String instructions
) {}
