package com.fitnessapp.exercises;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("muscle_groups")
@Getter
@Setter
public class MuscleGroup {

    @Id
    private UUID id;

    private String name;

    private String description;
}
