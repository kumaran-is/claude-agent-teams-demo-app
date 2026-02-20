package com.fitnessapp.workouts;

import com.fitnessapp.common.PagedResponse;
import com.fitnessapp.security.FirebaseAuthentication;
import com.fitnessapp.workouts.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    /**
     * POST /api/v1/workouts/sessions — start a new session.
     */
    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SessionResponse> startSession(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody(required = false) CreateSessionRequest request) {
        return workoutService.startSession(auth, request);
    }

    /**
     * GET /api/v1/workouts/sessions — list user's sessions (paginated).
     */
    @GetMapping("/sessions")
    public Mono<PagedResponse<SessionResponse>> listSessions(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return workoutService.listSessions(auth, page, size);
    }

    /**
     * GET /api/v1/workouts/sessions/{sessionId} — get session with all sets.
     */
    @GetMapping("/sessions/{sessionId}")
    public Mono<SessionResponse> getSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return workoutService.getSession(sessionId, auth);
    }

    /**
     * POST /api/v1/workouts/sessions/{sessionId}/sets — log a set.
     */
    @PostMapping("/sessions/{sessionId}/sets")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SetResponse> logSet(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody LogSetRequest request) {
        return workoutService.logSet(sessionId, auth, request);
    }

    /**
     * PATCH /api/v1/workouts/sessions/{sessionId}/complete — mark session as completed.
     */
    @PatchMapping("/sessions/{sessionId}/complete")
    public Mono<SessionResponse> completeSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return workoutService.completeSession(sessionId, auth);
    }

    /**
     * GET /api/v1/workouts/metrics — aggregated metrics (volume, streak, PRs).
     */
    @GetMapping("/metrics")
    public Mono<MetricsResponse> getMetrics(
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return workoutService.getMetrics(auth);
    }
}
