package com.fitnessapp.goals;

import com.fitnessapp.goals.dto.CreateGoalRequest;
import com.fitnessapp.goals.dto.GoalResponse;
import com.fitnessapp.security.FirebaseAuthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public Flux<GoalResponse> listGoals(@AuthenticationPrincipal FirebaseAuthentication auth) {
        return goalService.listGoals(auth);
    }

    @GetMapping("/{id}")
    public Mono<GoalResponse> getGoal(
            @PathVariable UUID id,
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return goalService.getGoal(id, auth);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GoalResponse> createGoal(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody CreateGoalRequest request) {
        return goalService.createGoal(auth, request);
    }

    @PutMapping("/{id}")
    public Mono<GoalResponse> updateGoal(
            @PathVariable UUID id,
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody CreateGoalRequest request) {
        return goalService.updateGoal(id, auth, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGoal(
            @PathVariable UUID id,
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return goalService.deleteGoal(id, auth);
    }
}
