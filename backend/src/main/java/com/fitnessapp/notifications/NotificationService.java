package com.fitnessapp.notifications;

import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.common.UserContextService;
import com.fitnessapp.notifications.dto.NotificationPreferenceRequest;
import com.fitnessapp.notifications.dto.PreferenceResponse;
import com.fitnessapp.notifications.dto.RegisterTokenRequest;
import com.fitnessapp.security.FirebaseAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserContextService userContextService;
    private final FCMService fcmService;

    public Mono<PreferenceResponse> registerToken(FirebaseAuthentication auth, RegisterTokenRequest request) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> preferenceRepository.findByUserId(userId)
                        .defaultIfEmpty(newPreference(userId))
                        .flatMap(pref -> {
                            pref.setFcmToken(request.fcmToken());
                            pref.setRemindersEnabled(request.remindersEnabled());
                            if (request.reminderTime() != null) {
                                pref.setReminderTime(request.reminderTime());
                            } else if (pref.getReminderTime() == null) {
                                pref.setReminderTime(LocalTime.of(8, 0));
                            }
                            pref.setUpdatedAt(Instant.now());
                            return preferenceRepository.save(pref);
                        })
                )
                .map(this::toResponse);
    }

    public Mono<PreferenceResponse> getPreferences(FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> preferenceRepository.findByUserId(userId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                "No notification preferences found. Register a device token first.")))
                )
                .map(this::toResponse);
    }

    public Mono<PreferenceResponse> updatePreferences(FirebaseAuthentication auth, NotificationPreferenceRequest request) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> preferenceRepository.findByUserId(userId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                "No notification preferences found. Register a device token first.")))
                        .flatMap(pref -> {
                            pref.setRemindersEnabled(request.remindersEnabled());
                            if (request.reminderTime() != null) {
                                pref.setReminderTime(request.reminderTime());
                            }
                            pref.setUpdatedAt(Instant.now());
                            return preferenceRepository.save(pref);
                        })
                )
                .map(this::toResponse);
    }

    public Mono<Void> deleteToken(FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> preferenceRepository.findByUserId(userId)
                        .flatMap(pref -> preferenceRepository.delete(pref))
                        .then()
                );
    }

    public Mono<Void> sendTest(FirebaseAuthentication auth) {
        return userContextService.resolveUserId(auth)
                .flatMap(userId -> preferenceRepository.findByUserId(userId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                "No device token registered")))
                )
                .flatMap(pref -> fcmService.send(
                        pref.getFcmToken(),
                        "Test Notification",
                        "Your push notifications are working!"
                ))
                .then();
    }

    private NotificationPreference newPreference(java.util.UUID userId) {
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        pref.setRemindersEnabled(false);
        pref.setReminderTime(LocalTime.of(8, 0));
        return pref;
    }

    private PreferenceResponse toResponse(NotificationPreference pref) {
        return new PreferenceResponse(
                pref.getId(), pref.isRemindersEnabled(), pref.getReminderTime(), pref.getUpdatedAt()
        );
    }
}
