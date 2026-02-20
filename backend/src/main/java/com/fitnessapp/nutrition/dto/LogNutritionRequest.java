package com.fitnessapp.nutrition.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LogNutritionRequest(
        @NotNull LocalDate loggedAt,
        @NotBlank @Size(max = 255) String mealName,
        @NotNull @DecimalMin("0") BigDecimal calories,
        @NotNull @DecimalMin("0") BigDecimal proteinG,
        @NotNull @DecimalMin("0") BigDecimal carbsG,
        @NotNull @DecimalMin("0") BigDecimal fatG,
        @Size(max = 1000) String notes
) {}
