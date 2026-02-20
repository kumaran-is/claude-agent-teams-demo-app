package com.fitnessapp.workouts;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("workout_sets")
@Getter
@Setter
public class WorkoutSet {

    @Id
    private UUID id;

    private UUID sessionId;

    private UUID exerciseId;

    private int setNumber;

    private int reps;

    private BigDecimal weightKg;

    private Instant completedAt;
}
