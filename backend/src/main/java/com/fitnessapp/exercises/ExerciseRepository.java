package com.fitnessapp.exercises;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ExerciseRepository extends ReactiveCrudRepository<Exercise, UUID> {

    /**
     * Filter by muscle group (exact match).
     */
    Flux<Exercise> findAllByMuscleGroupId(UUID muscleGroupId, Pageable pageable);

    /**
     * No muscle group filter — paginated full list.
     */
    Flux<Exercise> findAllBy(Pageable pageable);

    /**
     * Case-insensitive name search, no muscle group filter.
     */
    @Query("SELECT * FROM exercises WHERE LOWER(name) LIKE '%' || LOWER(:search) || '%' ORDER BY name LIMIT :limit OFFSET :offset")
    Flux<Exercise> searchByName(String search, int limit, long offset);

    /**
     * Case-insensitive name search with muscle group filter.
     */
    @Query("SELECT * FROM exercises WHERE LOWER(name) LIKE '%' || LOWER(:search) || '%' AND muscle_group_id = :muscleGroupId ORDER BY name LIMIT :limit OFFSET :offset")
    Flux<Exercise> searchByNameAndMuscleGroup(String search, UUID muscleGroupId, int limit, long offset);

    Mono<Long> countByMuscleGroupId(UUID muscleGroupId);

    @Query("SELECT COUNT(*) FROM exercises WHERE LOWER(name) LIKE '%' || LOWER(:search) || '%'")
    Mono<Long> countByNameSearch(String search);

    @Query("SELECT COUNT(*) FROM exercises WHERE LOWER(name) LIKE '%' || LOWER(:search) || '%' AND muscle_group_id = :muscleGroupId")
    Mono<Long> countByNameSearchAndMuscleGroup(String search, UUID muscleGroupId);
}
