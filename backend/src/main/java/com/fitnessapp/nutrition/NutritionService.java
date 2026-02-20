package com.fitnessapp.nutrition;

import com.fitnessapp.common.ForbiddenException;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.common.UserContextService;
import com.fitnessapp.nutrition.dto.LogNutritionRequest;
import com.fitnessapp.nutrition.dto.NutritionLogResponse;
import com.fitnessapp.nutrition.dto.NutritionSummaryResponse;
import com.fitnessapp.security.FirebaseAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionService {

    private final NutritionLogRepository nutritionLogRepository;
    private final UserContextService userContextService;

    public Mono<NutritionLogResponse> logNutrition(FirebaseAuthentication auth, LogNutritionRequest request) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> {
                    NutritionLog log = new NutritionLog();
                    log.setUserId(userId);
                    log.setLoggedAt(request.loggedAt());
                    log.setMealName(request.mealName());
                    log.setCalories(request.calories());
                    log.setProteinG(request.proteinG());
                    log.setCarbsG(request.carbsG());
                    log.setFatG(request.fatG());
                    log.setNotes(request.notes());
                    return nutritionLogRepository.save(log);
                })
                .map(this::toResponse);
    }

    /**
     * Returns logs filtered by:
     * - exact date (date param), OR
     * - date range (both from AND to must be provided — supplying only one is a 400),OR
     * - no filter (all logs for the user)
     */
    public Flux<NutritionLogResponse> getLogs(
            FirebaseAuthentication auth, LocalDate date, LocalDate from, LocalDate to) {

        // M2: validate that from/to are either both present or both absent
        if ((from == null) != (to == null)) {
            return Flux.error(new IllegalArgumentException(
                    "Both 'from' and 'to' parameters must be supplied together for a date range filter"));
        }

        return userContextService.resolveUserId(auth)
                .flatMapMany(userId -> {
                    if (date != null) {
                        return nutritionLogRepository.findByUserIdAndDate(userId, date);
                    } else if (from != null) {
                        return nutritionLogRepository.findByUserIdAndDateRange(userId, from, to);
                    } else {
                        return nutritionLogRepository.findAllByUserIdOrderByLoggedAtDesc(userId);
                    }
                })
                .map(this::toResponse);
    }

    public Mono<NutritionSummaryResponse> getSummary(FirebaseAuthentication auth, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> nutritionLogRepository.sumByUserIdAndDate(userId, targetDate))
                .map(proj -> new NutritionSummaryResponse(
                        targetDate,
                        nullSafe(proj.getTotalCalories()),
                        nullSafe(proj.getTotalProteinG()),
                        nullSafe(proj.getTotalCarbsG()),
                        nullSafe(proj.getTotalFatG())
                ))
                .defaultIfEmpty(new NutritionSummaryResponse(
                        targetDate, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public Mono<Void> deleteLog(UUID id, FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> nutritionLogRepository.findById(id)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Nutrition log not found: " + id)))
                        .flatMap(log -> {
                            if (!log.getUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException("Access denied"));
                            }
                            return nutritionLogRepository.delete(log);
                        })
                );
    }

    private NutritionLogResponse toResponse(NutritionLog log) {
        return new NutritionLogResponse(
                log.getId(), log.getLoggedAt(), log.getMealName(),
                log.getCalories(), log.getProteinG(), log.getCarbsG(),
                log.getFatG(), log.getNotes()
        );
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
