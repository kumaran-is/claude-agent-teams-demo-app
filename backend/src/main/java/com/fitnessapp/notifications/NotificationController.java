package com.fitnessapp.notifications;

import com.fitnessapp.notifications.dto.NotificationPreferenceRequest;
import com.fitnessapp.notifications.dto.PreferenceResponse;
import com.fitnessapp.notifications.dto.RegisterTokenRequest;
import com.fitnessapp.security.FirebaseAuthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * POST /api/v1/notifications/token — register or update FCM token + preferences.
     */
    @PostMapping("/token")
    public Mono<PreferenceResponse> registerToken(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody RegisterTokenRequest request) {
        return notificationService.registerToken(auth, request);
    }

    /**
     * DELETE /api/v1/notifications/token — unregister FCM token (on logout).
     */
    @DeleteMapping("/token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteToken(@AuthenticationPrincipal FirebaseAuthentication auth) {
        return notificationService.deleteToken(auth);
    }

    /**
     * GET /api/v1/notifications/preferences — get current notification preferences.
     */
    @GetMapping("/preferences")
    public Mono<PreferenceResponse> getPreferences(
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return notificationService.getPreferences(auth);
    }

    /**
     * PUT /api/v1/notifications/preferences — update reminder settings.
     */
    @PutMapping("/preferences")
    public Mono<PreferenceResponse> updatePreferences(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        return notificationService.updatePreferences(auth, request);
    }

    /**
     * POST /api/v1/notifications/test — send a test push notification to verify setup.
     */
    @PostMapping("/test")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> sendTest(@AuthenticationPrincipal FirebaseAuthentication auth) {
        return notificationService.sendTest(auth);
    }
}
