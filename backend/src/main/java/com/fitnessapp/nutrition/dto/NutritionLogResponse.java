package com.fitnessapp.nutrition.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record NutritionLogResponse(
        UUID id,
        LocalDate loggedAt,
        String mealName,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG,
        String notes
) {}
