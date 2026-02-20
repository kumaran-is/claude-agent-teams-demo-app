package com.fitnessapp.workouts;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * R2DBC projection for personal record (max weight per exercise).
 */
public interface PersonalRecordProjection {

    UUID getExerciseId();

    BigDecimal getMaxWeightKg();
}
