package com.fitnessapp.workouts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LogSetRequest(
        @NotNull UUID exerciseId,
        @Min(1) int setNumber,
        @Min(1) int reps,
        @NotNull @DecimalMin("0") BigDecimal weightKg
) {}
