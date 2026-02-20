package com.fitnessapp.workouts;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface WorkoutSessionRepository extends ReactiveCrudRepository<WorkoutSession, UUID> {

    Flux<WorkoutSession> findAllByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    Mono<Long> countByUserId(UUID userId);

    @Query("""
            SELECT COUNT(*) FROM workout_sessions
            WHERE user_id = :userId AND started_at >= :since
            """)
    Mono<Long> countByUserIdAndStartedAtAfter(UUID userId, Instant since);

    /**
     * Used for current_streak calculation — returns distinct dates with sessions, newest first.
     */
    @Query("""
            SELECT DISTINCT DATE(started_at AT TIME ZONE 'UTC') AS session_date
            FROM workout_sessions
            WHERE user_id = :userId AND completed_at IS NOT NULL
            ORDER BY session_date DESC
            LIMIT 365
            """)
    Flux<java.time.LocalDate> findDistinctSessionDatesByUserId(UUID userId);
}
