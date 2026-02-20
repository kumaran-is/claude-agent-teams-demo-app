package com.fitnessapp.workouts.dto;

import jakarta.validation.constraints.Size;

public record CreateSessionRequest(
        @Size(max = 255) String name,
        @Size(max = 2000) String notes
) {}
