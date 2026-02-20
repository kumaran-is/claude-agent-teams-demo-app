package com.fitnessapp.goals;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface GoalRepository extends ReactiveCrudRepository<Goal, UUID> {

    Flux<Goal> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
