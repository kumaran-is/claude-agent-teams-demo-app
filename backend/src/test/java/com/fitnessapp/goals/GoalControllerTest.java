package com.fitnessapp.goals;

import com.fitnessapp.common.ForbiddenException;
import com.fitnessapp.common.GlobalExceptionHandler;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.goals.dto.GoalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(GoalController.class)
@Import(GlobalExceptionHandler.class)
class GoalControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GoalService goalService;

    private GoalResponse sampleGoal;
    private UUID goalId;

    @BeforeEach
    void setUp() {
        goalId = UUID.randomUUID();
        sampleGoal = new GoalResponse(
                goalId, GoalType.TARGET_WEIGHT, BigDecimal.valueOf(75.0),
                "kg", LocalDate.now().plusMonths(3), Instant.now(), Instant.now()
        );
    }

    // ── GET /api/v1/goals ────────────────────────────────────────────────────

    @Test
    void listGoals_authenticated_returns200() {
        when(goalService.listGoals(any())).thenReturn(Flux.just(sampleGoal));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/goals")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GoalResponse.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).goalType()).isEqualTo(GoalType.TARGET_WEIGHT));
    }

    @Test
    void listGoals_unauthenticated_returns401() {
        webTestClient
                .get().uri("/api/v1/goals")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── GET /api/v1/goals/{id} ───────────────────────────────────────────────

    @Test
    void getGoal_found_returns200() {
        when(goalService.getGoal(eq(goalId), any())).thenReturn(Mono.just(sampleGoal));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/goals/{id}", goalId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GoalResponse.class)
                .value(r -> assertThat(r.id()).isEqualTo(goalId));
    }

    @Test
    void getGoal_notFound_returns404() {
        UUID otherId = UUID.randomUUID();
        when(goalService.getGoal(eq(otherId), any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("Goal not found: " + otherId)));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/goals/{id}", otherId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getGoal_differentUser_returns403() {
        when(goalService.getGoal(eq(goalId), any()))
                .thenReturn(Mono.error(new ForbiddenException("Access denied")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("other-uid").roles("USER"))
                .get().uri("/api/v1/goals/{id}", goalId)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ── POST /api/v1/goals ───────────────────────────────────────────────────

    @Test
    void createGoal_authenticated_returns201() {
        when(goalService.createGoal(any(), any())).thenReturn(Mono.just(sampleGoal));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .post().uri("/api/v1/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"goalType":"TARGET_WEIGHT","targetValue":75.0,"unit":"kg",
                         "targetDate":"2025-04-01"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(GoalResponse.class)
                .value(r -> assertThat(r.targetValue()).isEqualByComparingTo(BigDecimal.valueOf(75.0)));
    }

    @Test
    void createGoal_unauthenticated_returns401() {
        webTestClient
                .post().uri("/api/v1/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"goalType\":\"TARGET_WEIGHT\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── PUT /api/v1/goals/{id} ───────────────────────────────────────────────

    @Test
    void updateGoal_authenticated_returns200() {
        when(goalService.updateGoal(eq(goalId), any(), any())).thenReturn(Mono.just(sampleGoal));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .put().uri("/api/v1/goals/{id}", goalId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"goalType":"TARGET_WEIGHT","targetValue":73.0,"unit":"kg",
                         "targetDate":"2025-05-01"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    // ── DELETE /api/v1/goals/{id} ────────────────────────────────────────────

    @Test
    void deleteGoal_authenticated_returns204() {
        when(goalService.deleteGoal(eq(goalId), any())).thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .delete().uri("/api/v1/goals/{id}", goalId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteGoal_differentUser_returns403() {
        when(goalService.deleteGoal(eq(goalId), any()))
                .thenReturn(Mono.error(new ForbiddenException("Access denied")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("other-uid").roles("USER"))
                .delete().uri("/api/v1/goals/{id}", goalId)
                .exchange()
                .expectStatus().isForbidden();
    }
}
