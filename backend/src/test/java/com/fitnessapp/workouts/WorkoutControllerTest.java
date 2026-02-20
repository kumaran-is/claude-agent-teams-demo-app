package com.fitnessapp.workouts;

import com.fitnessapp.common.GlobalExceptionHandler;
import com.fitnessapp.common.PagedResponse;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.common.ForbiddenException;
import com.fitnessapp.workouts.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(WorkoutController.class)
@Import(GlobalExceptionHandler.class)
class WorkoutControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WorkoutService workoutService;

    private SessionResponse sampleSession;
    private SetResponse sampleSet;
    private UUID sessionId;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        sampleSet = new SetResponse(UUID.randomUUID(), UUID.randomUUID(), 1, 10,
                BigDecimal.valueOf(60.0), Instant.now());
        sampleSession = new SessionResponse(sessionId, "Morning Workout",
                Instant.now(), null, null, List.of(sampleSet));
    }

    // ── POST /api/v1/workouts/sessions ──────────────────────────────────────

    @Test
    void startSession_authenticated_returns201() {
        when(workoutService.startSession(any(), any())).thenReturn(Mono.just(sampleSession));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .post().uri("/api/v1/workouts/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Morning Workout\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SessionResponse.class)
                .value(r -> assertThat(r.name()).isEqualTo("Morning Workout"));
    }

    @Test
    void startSession_unauthenticated_returns401() {
        webTestClient
                .post().uri("/api/v1/workouts/sessions")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── GET /api/v1/workouts/sessions ────────────────────────────────────────

    @Test
    void listSessions_authenticated_returns200() {
        PagedResponse<SessionResponse> page = new PagedResponse<>(List.of(sampleSession), 0, 20, 1L);
        when(workoutService.listSessions(any(), eq(0), eq(20))).thenReturn(Mono.just(page));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/workouts/sessions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].id").isEqualTo(sessionId.toString())
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    void listSessions_unauthenticated_returns401() {
        webTestClient
                .get().uri("/api/v1/workouts/sessions")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── GET /api/v1/workouts/sessions/{id} ───────────────────────────────────

    @Test
    void getSession_found_returns200() {
        when(workoutService.getSession(eq(sessionId), any())).thenReturn(Mono.just(sampleSession));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/workouts/sessions/{id}", sessionId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getSession_notFound_returns404() {
        UUID otherId = UUID.randomUUID();
        when(workoutService.getSession(eq(otherId), any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("Session not found: " + otherId)));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/workouts/sessions/{id}", otherId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getSession_differentUser_returns403() {
        when(workoutService.getSession(eq(sessionId), any()))
                .thenReturn(Mono.error(new ForbiddenException("Access denied")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("other-uid").roles("USER"))
                .get().uri("/api/v1/workouts/sessions/{id}", sessionId)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ── POST /api/v1/workouts/sessions/{id}/sets ─────────────────────────────

    @Test
    void logSet_authenticated_returns201() {
        when(workoutService.logSet(eq(sessionId), any(), any())).thenReturn(Mono.just(sampleSet));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .post().uri("/api/v1/workouts/sessions/{id}/sets", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"exerciseId":"%s","setNumber":1,"reps":10,"weightKg":60.0}
                        """.formatted(UUID.randomUUID()))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void logSet_unauthenticated_returns401() {
        webTestClient
                .post().uri("/api/v1/workouts/sessions/{id}/sets", sessionId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── PATCH /api/v1/workouts/sessions/{id}/complete ────────────────────────

    @Test
    void completeSession_authenticated_returns200() {
        SessionResponse completed = new SessionResponse(sessionId, "Morning Workout",
                sampleSession.startedAt(), Instant.now(), null, List.of());
        when(workoutService.completeSession(eq(sessionId), any())).thenReturn(Mono.just(completed));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .patch().uri("/api/v1/workouts/sessions/{id}/complete", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SessionResponse.class)
                .value(r -> assertThat(r.completedAt()).isNotNull());
    }

    // ── GET /api/v1/workouts/metrics ─────────────────────────────────────────

    @Test
    void getMetrics_authenticated_returns200() {
        MetricsResponse metrics = new MetricsResponse(3L, BigDecimal.valueOf(5000), 5, Map.of());
        when(workoutService.getMetrics(any())).thenReturn(Mono.just(metrics));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/workouts/metrics")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MetricsResponse.class)
                .value(r -> assertThat(r.currentStreak()).isEqualTo(5));
    }

    @Test
    void getMetrics_unauthenticated_returns401() {
        webTestClient
                .get().uri("/api/v1/workouts/metrics")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
