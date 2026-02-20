package com.fitnessapp.auth;

import com.fitnessapp.auth.dto.RegisterRequest;
import com.fitnessapp.auth.dto.UserResponse;
import com.fitnessapp.security.FirebaseAuthentication;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
@Import(com.fitnessapp.common.GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    private FirebaseAuthentication mockAuth;
    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        // We can't easily construct a real FirebaseToken in unit tests,
        // so we mock UserService and set up a custom authentication token.
        sampleResponse = new UserResponse(
                UUID.randomUUID(),
                "test@example.com",
                "Test User",
                Instant.now()
        );
    }

    @Test
    void register_withValidToken_returns200() {
        when(userService.registerOrSync(any(), any())).thenReturn(Mono.just(sampleResponse));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("test-firebase-uid").roles("USER"))
                .post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RegisterRequest("Test User"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(response -> {
                    assertThat(response.email()).isEqualTo("test@example.com");
                    assertThat(response.displayName()).isEqualTo("Test User");
                });
    }

    @Test
    void register_withoutBody_returns200() {
        when(userService.registerOrSync(any(), any())).thenReturn(Mono.just(sampleResponse));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("test-firebase-uid").roles("USER"))
                .post().uri("/api/v1/auth/register")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void register_withoutAuthentication_returns401() {
        webTestClient
                .post().uri("/api/v1/auth/register")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getProfile_withValidToken_returns200() {
        when(userService.getProfile(any())).thenReturn(Mono.just(sampleResponse));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("test-firebase-uid").roles("USER"))
                .get().uri("/api/v1/auth/profile")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(response -> assertThat(response.email()).isEqualTo("test@example.com"));
    }

    @Test
    void getProfile_withoutAuthentication_returns401() {
        webTestClient
                .get().uri("/api/v1/auth/profile")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
