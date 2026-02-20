package com.fitnessapp.exercises;

import com.fitnessapp.common.GlobalExceptionHandler;
import com.fitnessapp.common.PagedResponse;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.exercises.dto.*;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(ExerciseController.class)
@Import(GlobalExceptionHandler.class)
class ExerciseControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ExerciseService exerciseService;

    private ExerciseResponse sampleExercise;
    private MuscleGroupResponse sampleMuscleGroup;

    @BeforeEach
    void setUp() {
        sampleExercise = new ExerciseResponse(
                UUID.randomUUID(), "Bench Press", "Chest exercise",
                UUID.randomUUID(), "Chest", "Press the bar up", null, Instant.now()
        );
        sampleMuscleGroup = new MuscleGroupResponse(UUID.randomUUID(), "Chest");
    }

    // ── GET /api/v1/exercises ────────────────────────────────────────────────

    @Test
    void listExercises_publicEndpoint_returns200() {
        PagedResponse<ExerciseResponse> page = new PagedResponse<>(List.of(sampleExercise), 0, 20, 1L);
        when(exerciseService.listExercises(any(), any(), eq(0), eq(20))).thenReturn(Mono.just(page));

        webTestClient
                .get().uri("/api/v1/exercises")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].name").isEqualTo("Bench Press")
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    void listExercises_invalidPageSize_returns400() {
        // size=0 violates @Min(1)
        webTestClient
                .get().uri("/api/v1/exercises?size=0")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ── GET /api/v1/exercises/{id} ───────────────────────────────────────────

    @Test
    void getExercise_found_returns200() {
        UUID id = sampleExercise.id();
        when(exerciseService.getById(id)).thenReturn(Mono.just(sampleExercise));

        webTestClient
                .get().uri("/api/v1/exercises/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExerciseResponse.class)
                .value(r -> assertThat(r.name()).isEqualTo("Bench Press"));
    }

    @Test
    void getExercise_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(exerciseService.getById(id))
                .thenReturn(Mono.error(new ResourceNotFoundException("Exercise not found: " + id)));

        webTestClient
                .get().uri("/api/v1/exercises/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── POST /api/v1/exercises ───────────────────────────────────────────────

    @Test
    void createExercise_authenticated_returns201() {
        when(exerciseService.create(any(), any())).thenReturn(Mono.just(sampleExercise));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .post().uri("/api/v1/exercises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Bench Press","muscleGroupId":"%s"}
                        """.formatted(UUID.randomUUID()))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void createExercise_unauthenticated_returns401() {
        webTestClient
                .post().uri("/api/v1/exercises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Bench Press\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── DELETE /api/v1/exercises/{id} ───────────────────────────────────────

    @Test
    void deleteExercise_authenticated_returns204() {
        UUID id = UUID.randomUUID();
        when(exerciseService.delete(eq(id), any())).thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .delete().uri("/api/v1/exercises/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteExercise_unauthenticated_returns401() {
        webTestClient
                .delete().uri("/api/v1/exercises/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── GET /api/v1/muscle-groups ────────────────────────────────────────────

    @Test
    void listMuscleGroups_publicEndpoint_returns200() {
        when(exerciseService.listMuscleGroups()).thenReturn(Flux.just(sampleMuscleGroup));

        webTestClient
                .get().uri("/api/v1/muscle-groups")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MuscleGroupResponse.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).name()).isEqualTo("Chest"));
    }
}
