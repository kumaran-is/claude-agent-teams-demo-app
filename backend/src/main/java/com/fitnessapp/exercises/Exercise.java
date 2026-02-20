package com.fitnessapp.exercises;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("exercises")
@Getter
@Setter
public class Exercise {

    @Id
    private UUID id;

    private String name;

    private String description;

    private UUID muscleGroupId;

    private String instructions;

    /** NULL for system exercises; set to creating user's ID for custom exercises. */
    private UUID createdByUserId;

    private Instant createdAt;
}
