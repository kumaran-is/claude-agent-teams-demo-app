package com.fitnessapp.notifications;

import com.fitnessapp.common.GlobalExceptionHandler;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.notifications.dto.PreferenceResponse;
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

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private NotificationService notificationService;

    private PreferenceResponse samplePreference;

    @BeforeEach
    void setUp() {
        samplePreference = new PreferenceResponse(
                UUID.randomUUID(), true, LocalTime.of(8, 0), Instant.now()
        );
    }

    // ── POST /api/v1/notifications/token ────────────────────────────────────

    @Test
    void registerToken_authenticated_returns200() {
        when(notificationService.registerToken(any(), any())).thenReturn(Mono.just(samplePreference));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .post().uri("/api/v1/notifications/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"fcmToken\":\"device-token-abc\",\"remindersEnabled\":true}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PreferenceResponse.class)
                .value(r -> assertThat(r.remindersEnabled()).isTrue());
    }

    @Test
    void registerToken_unauthenticated_returns401() {
        webTestClient
                .post().uri("/api/v1/notifications/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"fcmToken\":\"token\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── DELETE /api/v1/notifications/token ──────────────────────────────────

    @Test
    void deleteToken_authenticated_returns204() {
        when(notificationService.deleteToken(any())).thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .delete().uri("/api/v1/notifications/token")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteToken_unauthenticated_returns401() {
        webTestClient
                .delete().uri("/api/v1/notifications/token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── GET /api/v1/notifications/preferences ────────────────────────────────

    @Test
    void getPreferences_found_returns200() {
        when(notificationService.getPreferences(any())).thenReturn(Mono.just(samplePreference));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/notifications/preferences")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PreferenceResponse.class)
                .value(r -> assertThat(r.reminderTime()).isEqualTo(LocalTime.of(8, 0)));
    }

    @Test
    void getPreferences_notRegistered_returns404() {
        when(notificationService.getPreferences(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException(
                        "No notification preferences found. Register a device token first.")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .get().uri("/api/v1/notifications/preferences")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getPreferences_unauthenticated_returns401() {
        webTestClient
                .get().uri("/api/v1/notifications/preferences")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── PUT /api/v1/notifications/preferences ────────────────────────────────

    @Test
    void updatePreferences_authenticated_returns200() {
        when(notificationService.updatePreferences(any(), any())).thenReturn(Mono.just(samplePreference));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .put().uri("/api/v1/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"remindersEnabled\":true,\"reminderTime\":\"08:00\"}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updatePreferences_unauthenticated_returns401() {
        webTestClient
                .put().uri("/api/v1/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"remindersEnabled\":false}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── POST /api/v1/notifications/test ─────────────────────────────────────

    @Test
    void sendTest_authenticated_returns204() {
        when(notificationService.sendTest(any())).thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .post().uri("/api/v1/notifications/test")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void sendTest_notRegistered_returns404() {
        when(notificationService.sendTest(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("No device token registered")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("uid").roles("USER"))
                .post().uri("/api/v1/notifications/test")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void sendTest_unauthenticated_returns401() {
        webTestClient
                .post().uri("/api/v1/notifications/test")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
