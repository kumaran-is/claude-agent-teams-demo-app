package com.fitnessapp.workouts;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("workout_sessions")
@Getter
@Setter
public class WorkoutSession {

    @Id
    private UUID id;

    private UUID userId;

    private String name;

    private Instant startedAt;

    private Instant completedAt;

    private String notes;
}
