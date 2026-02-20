package com.fitnessapp.nutrition;

import com.fitnessapp.nutrition.dto.LogNutritionRequest;
import com.fitnessapp.nutrition.dto.NutritionLogResponse;
import com.fitnessapp.nutrition.dto.NutritionSummaryResponse;
import com.fitnessapp.security.FirebaseAuthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    @PostMapping("/logs")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<NutritionLogResponse> logNutrition(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody LogNutritionRequest request) {
        return nutritionService.logNutrition(auth, request);
    }

    /**
     * GET /api/v1/nutrition/logs?date=&from=&to=
     * Returns logs filtered by a specific date or a date range.
     * With no filters, returns all logs for the user.
     */
    @GetMapping("/logs")
    public Flux<NutritionLogResponse> getLogs(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return nutritionService.getLogs(auth, date, from, to);
    }

    @DeleteMapping("/logs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteLog(
            @PathVariable UUID id,
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return nutritionService.deleteLog(id, auth);
    }

    /**
     * GET /api/v1/nutrition/summary?date=
     * Returns macro totals for a given date (defaults to today).
     */
    @GetMapping("/summary")
    public Mono<NutritionSummaryResponse> getSummary(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return nutritionService.getSummary(auth, date);
    }
}
