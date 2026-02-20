package com.fitnessapp.nutrition;

import com.fitnessapp.common.ForbiddenException;
import com.fitnessapp.common.GlobalExceptionHandler;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.nutrition.dto.NutritionLogResponse;
import com.fitnessapp.nutrition.dto.NutritionSummaryResponse;
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
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(NutritionController.class)
@Import(GlobalExceptionHandler.class)
class NutritionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private NutritionService nutritionService;

    private NutritionLogResponse sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new NutritionLogResponse(
                UUID.randomUUID(), LocalDate.now(), "Lunch",
                BigDecimal.valueOf(600), BigDecimal.valueOf(40),
                BigDecimal.valueOf(70), BigDecimal.valueOf(15), null
        );
    }

    // ── POST /api/v1/nutrition/logs ──────────────────────────────────────────

    @Test
    void logNutrition_authenticated_returns201() {
        when(nutritionService.logNutrition(any(), any())).thenReturn(Mono.just(sampleLog));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .post().uri("/api/v1/nutrition/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"loggedAt":"2025-01-01","mealName":"Lunch","calories":600,
                         "proteinG":40,"carbsG":70,"fatG":15}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(NutritionLogResponse.class)
                .value(r -> assertThat(r.mealName()).isEqualTo("Lunch"));
    }

    @Test
    void logNutrition_unauthenticated_returns401() {
        webTestClient
                .post().uri("/api/v1/nutrition/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"mealName\":\"Lunch\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── GET /api/v1/nutrition/logs ───────────────────────────────────────────

    @Test
    void getLogs_authenticated_returns200() {
        when(nutritionService.getLogs(any(), any(), any(), any())).thenReturn(Flux.just(sampleLog));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/nutrition/logs")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(NutritionLogResponse.class)
                .hasSize(1);
    }

    @Test
    void getLogs_onlyFromParam_returns400() {
        // Service throws IllegalArgumentException if only one of from/to is supplied
        when(nutritionService.getLogs(any(), any(), any(), any()))
                .thenReturn(Flux.error(new IllegalArgumentException(
                        "Both 'from' and 'to' parameters must be supplied together for a date range filter")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/nutrition/logs?from=2025-01-01")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getLogs_unauthenticated_returns401() {
        webTestClient
                .get().uri("/api/v1/nutrition/logs")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── DELETE /api/v1/nutrition/logs/{id} ───────────────────────────────────

    @Test
    void deleteLog_authenticated_returns204() {
        UUID id = UUID.randomUUID();
        when(nutritionService.deleteLog(eq(id), any())).thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .delete().uri("/api/v1/nutrition/logs/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteLog_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(nutritionService.deleteLog(eq(id), any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("Nutrition log not found: " + id)));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .delete().uri("/api/v1/nutrition/logs/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteLog_differentUser_returns403() {
        UUID id = UUID.randomUUID();
        when(nutritionService.deleteLog(eq(id), any()))
                .thenReturn(Mono.error(new ForbiddenException("Access denied")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("other-uid").roles("USER"))
                .delete().uri("/api/v1/nutrition/logs/{id}", id)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ── GET /api/v1/nutrition/summary ────────────────────────────────────────

    @Test
    void getSummary_authenticated_returns200() {
        NutritionSummaryResponse summary = new NutritionSummaryResponse(
                LocalDate.now(), BigDecimal.valueOf(1800),
                BigDecimal.valueOf(120), BigDecimal.valueOf(200), BigDecimal.valueOf(60)
        );
        when(nutritionService.getSummary(any(), any())).thenReturn(Mono.just(summary));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/nutrition/summary")
                .exchange()
                .expectStatus().isOk()
                .expectBody(NutritionSummaryResponse.class)
                .value(r -> assertThat(r.totalCalories()).isEqualByComparingTo(BigDecimal.valueOf(1800)));
    }

    @Test
    void getSummary_unauthenticated_returns401() {
        webTestClient
                .get().uri("/api/v1/nutrition/summary")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
