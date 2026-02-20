package com.fitnessapp.exercises;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface MuscleGroupRepository extends ReactiveCrudRepository<MuscleGroup, UUID> {
}
