package com.fitnessapp.goals;

import com.fitnessapp.common.ForbiddenException;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.common.UserContextService;
import com.fitnessapp.goals.dto.CreateGoalRequest;
import com.fitnessapp.goals.dto.GoalResponse;
import com.fitnessapp.security.FirebaseAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserContextService userContextService;

    public Flux<GoalResponse> listGoals(FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMapMany(userId -> goalRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .map(this::toResponse);
    }

    public Mono<GoalResponse> getGoal(UUID id, FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> goalRepository.findById(id)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Goal not found: " + id)))
                        .flatMap(goal -> {
                            if (!goal.getUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException("Access denied"));
                            }
                            return Mono.just(goal);
                        })
                )
                .map(this::toResponse);
    }

    public Mono<GoalResponse> createGoal(FirebaseAuthentication auth, CreateGoalRequest request) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> {
                    Goal goal = new Goal();
                    goal.setUserId(userId);
                    goal.setGoalType(request.goalType());
                    goal.setTargetValue(request.targetValue());
                    goal.setUnit(request.unit());
                    goal.setTargetDate(request.targetDate());
                    goal.setCreatedAt(Instant.now());
                    goal.setUpdatedAt(Instant.now());
                    return goalRepository.save(goal);
                })
                .map(this::toResponse);
    }

    public Mono<GoalResponse> updateGoal(UUID id, FirebaseAuthentication auth, CreateGoalRequest request) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> goalRepository.findById(id)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Goal not found: " + id)))
                        .flatMap(goal -> {
                            if (!goal.getUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException("Access denied"));
                            }
                            goal.setGoalType(request.goalType());
                            goal.setTargetValue(request.targetValue());
                            goal.setUnit(request.unit());
                            goal.setTargetDate(request.targetDate());
                            goal.setUpdatedAt(Instant.now());
                            return goalRepository.save(goal);
                        })
                )
                .map(this::toResponse);
    }

    public Mono<Void> deleteGoal(UUID id, FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> goalRepository.findById(id)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Goal not found: " + id)))
                        .flatMap(goal -> {
                            if (!goal.getUserId().equals(userId)) {
                                return Mono.error(new ForbiddenException("Access denied"));
                            }
                            return goalRepository.delete(goal);
                        })
                );
    }

    private GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(), goal.getGoalType(), goal.getTargetValue(),
                goal.getUnit(), goal.getTargetDate(), goal.getCreatedAt(), goal.getUpdatedAt()
        );
    }
}
