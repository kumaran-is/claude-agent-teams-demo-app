package com.fitnessapp.nutrition;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface NutritionLogRepository extends ReactiveCrudRepository<NutritionLog, UUID> {

    Flux<NutritionLog> findAllByUserIdOrderByLoggedAtDesc(UUID userId);

    @Query("SELECT * FROM nutrition_logs WHERE user_id = :userId AND logged_at = :date ORDER BY meal_name")
    Flux<NutritionLog> findByUserIdAndDate(UUID userId, LocalDate date);

    @Query("SELECT * FROM nutrition_logs WHERE user_id = :userId AND logged_at BETWEEN :from AND :to ORDER BY logged_at DESC")
    Flux<NutritionLog> findByUserIdAndDateRange(UUID userId, LocalDate from, LocalDate to);

    @Query("""
            SELECT
                SUM(calories)  AS total_calories,
                SUM(protein_g) AS total_protein_g,
                SUM(carbs_g)   AS total_carbs_g,
                SUM(fat_g)     AS total_fat_g
            FROM nutrition_logs
            WHERE user_id = :userId AND logged_at = :date
            """)
    Mono<NutritionSummaryProjection> sumByUserIdAndDate(UUID userId, LocalDate date);
}
