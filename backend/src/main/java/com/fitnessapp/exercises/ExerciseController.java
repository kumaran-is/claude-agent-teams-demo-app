package com.fitnessapp.exercises;

import com.fitnessapp.common.PagedResponse;
import com.fitnessapp.exercises.dto.*;
import com.fitnessapp.security.FirebaseAuthentication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
public class ExerciseController {

    private final ExerciseService exerciseService;

    /**
     * GET /api/v1/exercises?muscleGroup=&search=&page=&size=
     * Public endpoint — no authentication required for browsing.
     * Page size clamped to max 100.
     */
    @GetMapping("/api/v1/exercises")
    public Mono<PagedResponse<ExerciseResponse>> listExercises(
            @RequestParam(required = false) UUID muscleGroup,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return exerciseService.listExercises(muscleGroup, search, page, size);
    }

    /**
     * GET /api/v1/exercises/{id}
     * Public endpoint.
     */
    @GetMapping("/api/v1/exercises/{id}")
    public Mono<ExerciseResponse> getExercise(@PathVariable UUID id) {
        return exerciseService.getById(id);
    }

    /**
     * POST /api/v1/exercises — create custom exercise (authenticated).
     */
    @PostMapping("/api/v1/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ExerciseResponse> createExercise(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody CreateExerciseRequest request) {
        return exerciseService.create(auth, request);
    }

    /**
     * PUT /api/v1/exercises/{id} — update exercise (creator only).
     */
    @PutMapping("/api/v1/exercises/{id}")
    public Mono<ExerciseResponse> updateExercise(
            @PathVariable UUID id,
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody UpdateExerciseRequest request) {
        return exerciseService.update(id, auth, request);
    }

    /**
     * DELETE /api/v1/exercises/{id} — delete exercise (creator only).
     */
    @DeleteMapping("/api/v1/exercises/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteExercise(
            @PathVariable UUID id,
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return exerciseService.delete(id, auth);
    }

    /**
     * GET /api/v1/muscle-groups — public endpoint.
     */
    @GetMapping("/api/v1/muscle-groups")
    public Flux<MuscleGroupResponse> listMuscleGroups() {
        return exerciseService.listMuscleGroups();
    }
}
