package com.fitnessapp.nutrition;

import java.math.BigDecimal;

/**
 * R2DBC projection for aggregated nutrition totals.
 */
public interface NutritionSummaryProjection {

    BigDecimal getTotalCalories();

    BigDecimal getTotalProteinG();

    BigDecimal getTotalCarbsG();

    BigDecimal getTotalFatG();
}
