package com.fitnessapp.goals.dto;

import com.fitnessapp.goals.GoalType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        GoalType goalType,
        BigDecimal targetValue,
        String unit,
        LocalDate targetDate,
        Instant createdAt,
        Instant updatedAt
) {}
