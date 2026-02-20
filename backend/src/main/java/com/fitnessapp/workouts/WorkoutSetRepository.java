package com.fitnessapp.workouts;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface WorkoutSetRepository extends ReactiveCrudRepository<WorkoutSet, UUID> {

    Flux<WorkoutSet> findAllBySessionIdOrderBySetNumber(UUID sessionId);

    /** Total volume (reps * weight_kg) for all sets in sessions completed this week. */
    @Query("""
            SELECT COALESCE(SUM(ws.reps * ws.weight_kg), 0)
            FROM workout_sets ws
            JOIN workout_sessions sess ON sess.id = ws.session_id
            WHERE sess.user_id = :userId AND sess.started_at >= :since
            """)
    Mono<BigDecimal> sumVolumeByUserIdAndStartedAtAfter(UUID userId, Instant since);

    /** Personal records: max weight per exercise for this user. */
    @Query("""
            SELECT ws.exercise_id, MAX(ws.weight_kg) AS max_weight_kg
            FROM workout_sets ws
            JOIN workout_sessions sess ON sess.id = ws.session_id
            WHERE sess.user_id = :userId
            GROUP BY ws.exercise_id
            """)
    Flux<PersonalRecordProjection> findPersonalRecordsByUserId(UUID userId);
}
