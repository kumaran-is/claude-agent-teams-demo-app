package com.fitnessapp.notifications;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.UUID;

public interface NotificationPreferenceRepository extends ReactiveCrudRepository<NotificationPreference, UUID> {

    Mono<NotificationPreference> findByUserId(UUID userId);

    /**
     * Find all users with reminders enabled and reminder_time matching the given hour (truncated to hour).
     * Used by the hourly scheduler to dispatch FCM notifications.
     */
    @Query("""
            SELECT * FROM notification_preferences
            WHERE reminders_enabled = true
              AND EXTRACT(HOUR FROM reminder_time) = EXTRACT(HOUR FROM CAST(:reminderTime AS TIME))
              AND EXTRACT(MINUTE FROM reminder_time) = EXTRACT(MINUTE FROM CAST(:reminderTime AS TIME))
            """)
    Flux<NotificationPreference> findDueReminders(LocalTime reminderTime);
}
