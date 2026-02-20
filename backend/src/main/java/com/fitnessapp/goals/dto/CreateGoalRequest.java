package com.fitnessapp.goals.dto;

import com.fitnessapp.goals.GoalType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalRequest(
        @NotNull GoalType goalType,
        @NotNull @DecimalMin("0") BigDecimal targetValue,
        @NotBlank @Size(max = 50) String unit,
        LocalDate targetDate
) {}
