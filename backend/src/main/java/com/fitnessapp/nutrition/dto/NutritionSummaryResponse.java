package com.fitnessapp.nutrition.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NutritionSummaryResponse(
        LocalDate date,
        BigDecimal totalCalories,
        BigDecimal totalProteinG,
        BigDecimal totalCarbsG,
        BigDecimal totalFatG
) {}
